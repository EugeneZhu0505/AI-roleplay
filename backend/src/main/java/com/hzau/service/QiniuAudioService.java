package com.hzau.service;

import com.hzau.config.QiniuAiConfig;
import com.hzau.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: QiniuAudioService
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/24 下午9:23
 */
@Service
@Slf4j
public class QiniuAudioService {

    private final QiniuAiConfig config;
    private final WebClient webClient;
    private final ConcurrentControlService concurrentControlService;
    private final Executor llmRequestExecutor;
    private final QiniuUploadService qiniuUploadService;

    /**
     * 构造函数，初始化WebClient
     */
    public QiniuAudioService(QiniuAiConfig config,
                             ConcurrentControlService concurrentControlService,
                             @Qualifier("llmRequestExecutor") Executor llmRequestExecutor,
                             QiniuUploadService qiniuUploadService) {
        this.config = config;
        this.concurrentControlService = concurrentControlService;
        this.llmRequestExecutor = llmRequestExecutor;
        this.qiniuUploadService = qiniuUploadService;
        
        // 配置ExchangeStrategies以增加缓冲区大小限制
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
        
        // 配置HttpClient
        HttpClient httpClient = HttpClient.create();
        
        this.webClient = WebClient.builder()
                .baseUrl(config.getPrimaryEndpoint())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }


    /**
     * 语音转文本 (ASR) - 直接使用OSS URL
     * @param ossAudioUrl OSS音频文件URL
     * @param audioFormat 音频格式
     * @return 识别出的文本
     */
    public Mono<String> speechToTextFromOssUrl(String ossAudioUrl, String audioFormat) {
        log.info("开始语音转文本（OSS URL）, ossUrl: {}, format: {}", ossAudioUrl, audioFormat);
        
        // 验证输入参数
        if (ossAudioUrl == null || ossAudioUrl.trim().isEmpty()) {
            return Mono.error(new RuntimeException("OSS音频URL不能为空"));
        }
        
        if (audioFormat == null || audioFormat.trim().isEmpty()) {
            return Mono.error(new RuntimeException("音频格式不能为空"));
        }
        
        // 检查API配置
        if (!isConfigValid()) {
            return Mono.error(new RuntimeException("七牛云API配置无效"));
        }

        // 直接使用OSS URL进行语音转文本
        return speechToText(ossAudioUrl, audioFormat);
    }

    /**
     * 语音转文本 (ASR)
     * @param audioUrl 音频文件的公网URL
     * @param audioFormat 音频格式
     * @return 识别出的文本
     */
    public Mono<String> speechToText(String audioUrl, String audioFormat) {
        log.info("开始语音转文本, audioUrl: {}, format: {}", audioUrl, audioFormat);
        
        // 验证输入参数
        if (audioUrl == null || audioUrl.trim().isEmpty()) {
            return Mono.error(new RuntimeException("音频URL不能为空"));
        }
        
        if (audioFormat == null || audioFormat.trim().isEmpty()) {
            return Mono.error(new RuntimeException("音频格式不能为空"));
        }
        
        // 检查API配置
        if (!isConfigValid()) {
            return Mono.error(new RuntimeException("七牛云API配置无效"));
        }

        // 获取并发控制许可
        Mono<Boolean> permitMono = concurrentControlService.acquirePermit("system", "asr");
        
        // 构建ASR请求并发送
        Mono<String> asrRequestMono = permitMono.flatMap(permit -> {
            // 构建ASR请求
            AudioAsrReq request = new AudioAsrReq();
            request.setModel(config.getAsr().getModel());

            AudioAsrReq.AudioParam audioParam = new AudioAsrReq.AudioParam();
            audioParam.setFormat(audioFormat);
            audioParam.setUrl(audioUrl);
            request.setAudio(audioParam);

            return sendAsrRequest(request)
                    .subscribeOn(Schedulers.fromExecutor(llmRequestExecutor))
                    .map(this::extractAsrText);
        });
        
        // 处理成功响应
        Mono<String> successHandlerMono = asrRequestMono.doOnSuccess(result -> {
            log.info("语音转文本成功: {}", result);
            concurrentControlService.releasePermit("system", "asr");
        });
        
        // 处理错误响应
        Mono<String> errorHandlerMono = successHandlerMono.doOnError(error -> {
            log.error("语音转文本失败", error);
            concurrentControlService.releasePermit("system", "asr");
        });
        
        // 处理许可获取失败
        return errorHandlerMono.onErrorResume(error -> {
            return Mono.error(new RuntimeException("语音识别服务繁忙，请稍后重试"));
        });
    }

    /**
     * 文本转语音 (TTS)
     * @param text 需要合成的文本
     * @return base64编码的音频数据
     */
    public Mono<String> textToSpeech(String text) {
        return textToSpeech(text, config.getTts().getDefaultVoiceType(),
                config.getTts().getDefaultEncoding(),
                config.getTts().getDefaultSpeedRatio());
    }

    /**
     * 文本转语音 (TTS) - 自定义参数
     * @param text 需要合成的文本
     * @param voiceType 音色类型
     * @param encoding 音频编码
     * @param speedRatio 语速
     * @return base64编码的音频数据
     */
    public Mono<String> textToSpeech(String text, String voiceType, String encoding, Float speedRatio) {
        log.info("开始文本转语音, text: {}, voiceType: {}, encoding: {}, speedRatio: {}",
                text, voiceType, encoding, speedRatio);

        // 检查文本长度
        if (text.length() > config.getTts().getMaxTextLength()) {
            return Mono.error(new RuntimeException("文本长度超过限制: " + config.getTts().getMaxTextLength()));
        }

        // 获取并发控制许可
        return concurrentControlService.acquirePermit("system", "tts")
                .flatMap(permit -> {
                    // 构建TTS请求
                    AudioTtsReq request = new AudioTtsReq();

                    AudioTtsReq.AudioParam audioParam = new AudioTtsReq.AudioParam();
                    audioParam.setVoiceType(voiceType);
                    audioParam.setEncoding(encoding);
                    audioParam.setSpeedRatio(speedRatio);
                    request.setAudio(audioParam);

                    AudioTtsReq.RequestParam requestParam = new AudioTtsReq.RequestParam();
                    requestParam.setText(text);
                    request.setRequest(requestParam);

                    return sendTtsRequest(request)
                            .subscribeOn(Schedulers.fromExecutor(llmRequestExecutor))
                            .map(this::extractTtsAudioData)
                            .doOnSuccess(result -> {
                                log.info("文本转语音成功，音频数据长度: {}", result != null ? result.length() : 0);
                                concurrentControlService.releasePermit("system", "tts");
                            })
                            .doOnError(error -> {
                                log.error("文本转语音失败", error);
                                concurrentControlService.releasePermit("system", "tts");
                            });
                })
                .onErrorResume(error -> {
                    log.error("获取TTS并发许可失败", error);
                    return Mono.error(new RuntimeException("语音合成服务繁忙，请稍后重试"));
                });
    }

    /**
     * 获取音色列表
     * @return 音色列表
     */
    public Mono<AudioListRes> getVoiceList() {
        log.info("获取音色列表");

        // 发送HTTP请求获取音色列表
        Mono<AudioListRes> voiceListMono = webClient.get()
                .uri("/voice/list")
                .retrieve()
                .bodyToMono(AudioListRes.class);
        
        // 添加超时处理
        Mono<AudioListRes> timeoutHandlerMono = voiceListMono.timeout(Duration.ofSeconds(config.getTimeout()));
        
        // 添加重试机制
        Mono<AudioListRes> retryHandlerMono = timeoutHandlerMono.retryWhen(
                Retry.backoff(config.getMaxRetries(), Duration.ofSeconds(1))
                        .filter(throwable -> !(throwable instanceof WebClientResponseException.BadRequest))
        );
        
        // 添加错误日志记录
        Mono<AudioListRes> errorLogHandlerMono = retryHandlerMono.doOnError(error -> 
                log.error("获取音色列表失败", error)
        );
        
        // 处理错误映射
        return errorLogHandlerMono.onErrorMap(error ->
                new RuntimeException("获取音色列表失败: " + error.getMessage(), error)
        );
     }

     /**
      * 发送ASR请求到七牛云API
      */
     private Mono<AudioAsrRes> sendAsrRequest(AudioAsrReq request) {
         String endpoint = config.getPrimaryEndpoint() + "/voice/asr";
     
         Mono<AudioAsrRes> requestMono = webClient.post()
                 .uri(endpoint)
                 .header("Authorization", "Bearer " + config.getApiKey())
                 .bodyValue(request)
                 .retrieve()
                 .bodyToMono(AudioAsrRes.class);
     
         Mono<AudioAsrRes> timeoutMono = requestMono.timeout(Duration.ofSeconds(config.getTimeout()));
     
         Mono<AudioAsrRes> httpErrorLogMono = timeoutMono.doOnError(WebClientResponseException.class, ex -> {
             log.error("ASR请求失败，HTTP状态码: {}, 响应体: {}",
                     ex.getStatusCode(), ex.getResponseBodyAsString());
         });
     
         Mono<AudioAsrRes> otherErrorLogMono = httpErrorLogMono.doOnError(error -> !(error instanceof WebClientResponseException), error -> {
             log.error("ASR请求发生其他错误", error);
         });
     
         Mono<AudioAsrRes> finalMono = otherErrorLogMono.retryWhen(
                 Retry.backoff(config.getMaxRetries(), Duration.ofSeconds(2))
                         .filter(throwable -> !(throwable instanceof WebClientResponseException &&
                                 ((WebClientResponseException) throwable).getStatusCode().is4xxClientError()))
                         .doBeforeRetry(retrySignal -> {
                             log.warn("ASR请求重试，第{}次，原因: {}",
                                     retrySignal.totalRetries() + 1,
                                     retrySignal.failure().getMessage());
                         })
             );
     
         return finalMono;
     }

     /**
      * 发送TTS请求
      * @param request TTS请求
      * @return TTS响应
      */
     private Mono<AudioTtsRes> sendTtsRequest(AudioTtsReq request) {
         String endpoint = config.getPrimaryEndpoint() + "/voice/tts";
     
         Mono<AudioTtsRes> requestMono = webClient.post()
                 .uri(endpoint)
                 .header("Authorization", "Bearer " + config.getApiKey())
                 .bodyValue(request)
                 .retrieve()
                 .bodyToMono(AudioTtsRes.class);
     
         Mono<AudioTtsRes> timeoutMono = requestMono.timeout(Duration.ofSeconds(config.getTimeout()));
     
         Mono<AudioTtsRes> httpErrorLogMono = timeoutMono.doOnError(WebClientResponseException.class, ex -> {
             log.error("TTS请求失败，HTTP状态码: {}, 响应体: {}",
                     ex.getStatusCode(), ex.getResponseBodyAsString());
         });
     
         Mono<AudioTtsRes> otherErrorLogMono = httpErrorLogMono.doOnError(error -> !(error instanceof WebClientResponseException), error -> {
             log.error("TTS请求发生其他错误", error);
         });
     
         Mono<AudioTtsRes> finalMono = otherErrorLogMono.retryWhen(
                 Retry.backoff(config.getMaxRetries(), Duration.ofSeconds(2))
                         .filter(throwable -> !(throwable instanceof WebClientResponseException &&
                                 ((WebClientResponseException) throwable).getStatusCode().is4xxClientError()))
                         .doBeforeRetry(retrySignal -> {
                             log.warn("TTS请求重试，第{}次，原因: {}",
                                     retrySignal.totalRetries() + 1,
                                     retrySignal.failure().getMessage());
                         })
             );
     
         return finalMono;
     }

    /**
     * 从ASR响应中提取文本
     * @param response ASR响应
     * @return 识别出的文本
     */
    private String extractAsrText(AudioAsrRes response) {
        if (response == null) {
            throw new RuntimeException("ASR响应数据为空");
        }
        
        if (response.getData() == null) {
            log.error("ASR响应的data字段为null，完整响应: {}", response);
            throw new RuntimeException("ASR响应数据为空");
        }
        
        if (response.getData().getResult() == null) {
            log.error("ASR响应的result字段为null，data内容: {}", response.getData());
            throw new RuntimeException("ASR响应数据为空");
        }

        String text = response.getData().getResult().getText();
        
        if (text == null || text.trim().isEmpty()) {
            log.error("语音识别结果为空或空白，原始文本: '{}'", text);
            throw new RuntimeException("语音识别结果为空");
        }

        return text.trim();
    }

    /**
     * 从TTS响应中提取音频数据
     * @param response TTS响应
     * @return base64编码的音频数据
     */
    private String extractTtsAudioData(AudioTtsRes response) {
        if (response == null || response.getData() == null) {
            throw new RuntimeException("TTS响应数据为空");
        }

        String audioData = response.getData();
        if (audioData == null || audioData.trim().isEmpty()) {
            throw new RuntimeException("语音合成结果为空");
        }

        return audioData;
    }

    /**
     * 将base64音频数据转换为字节数组
     * @param base64AudioData base64编码的音频数据
     * @return 音频字节数组
     */
    public byte[] decodeAudioData(String base64AudioData) {
        try {
            return Base64.getDecoder().decode(base64AudioData);
        } catch (Exception e) {
            log.error("解码音频数据失败", e);
            throw new RuntimeException("音频数据解码失败", e);
        }
    }

    /**
     * 检查API配置是否有效
     * @return 是否有效
     */
    public boolean isConfigValid() {
        return config.getApiKey() != null &&
                !config.getApiKey().isEmpty() &&
                !"YOUR_API_KEY_HERE".equals(config.getApiKey());
    }
}
