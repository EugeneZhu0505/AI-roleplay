
/**
 * 向服务器发送请求的通用函数（React中使用）
 * @param {string} url - 请求地址
 * @param {string} method - 请求方法（GET/POST/PUT/DELETE等）
 * @param {object} [data] - 请求参数（GET会拼到URL，POST放在body）
 * @param {object} [headers] - 自定义请求头
 * @returns {Promise} - 服务器响应数据
 */

export const RegisterPost = async (url, body) => {
  try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
      });

      if (!response.ok) {
        throw new Error(`HTTP错误: 状态码 ${response.status}`);
      }
      return await response.json();
  } catch (error) {
    throw error; 
  }
}

// 登录请求
export const LoginPost = async (url, body) => {
  try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
      });

      if (!response.ok){
        throw new Error(`HTTP错误: 状态码 ${response.status}`);
      }
      return await response.json()
  } catch(error){
    throw error;
  }
}

// 无body的getAPI
export const getApiNotBody = async(url) => {
    try{
    const response = await fetch(url,{
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'accessToken' : JSON.parse(localStorage.getItem("login-success-user")).accessToken,
      },
    })

    if(!response.ok){
      throw new Error(`HTTP错误: 状态码 ${response.status}`);
    }
    return await response.json()
  }catch (error){
    throw error;
  }
}

// 创建新对话
export const createConversationPost = async (url) => {
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'accessToken' : JSON.parse(localStorage.getItem("login-success-user")).accessToken,
      },
    });
    if (!response.ok) {
      throw new Error(`HTTP错误: 状态码 ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    throw error;
  }
}

export const activeConversationPost = async (url) => {
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'accessToken' : JSON.parse(localStorage.getItem("login-success-user")).accessToken,
      },
    });
    if (!response.ok) {
      throw new Error(`HTTP错误: 状态码 ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    throw error;
  }
}

// 发送消息
export const sendMessagePost = async (url, body) => {
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
        'accessToken' : JSON.parse(localStorage.getItem("login-success-user")).accessToken,
      },
      body: JSON.stringify(body),
    });
    if (!response.ok) {
      throw new Error(`HTTP错误: 状态码 ${response.status}`);
    }

    // 创建一个流的读取器，并锁定当前的流
    return response.body.getReader();
  } catch (error) {
    throw error;
  }
}

export const sendAudioPost = async (url, body) => {
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Accept': '*/*',
        'accessToken' : JSON.parse(localStorage.getItem("login-success-user")).accessToken,
      },
      body: body,
    });
    if (!response.ok) {
      throw new Error(`HTTP错误: 状态码 ${response.status}`);
    }

    const data = await response.json();
    if(data.data.aiAudioUrl === null){
      throw new Error(`HTTP错误: 状态码 ${response.status}`);
    }
    return data.data.aiAudioUrl;

  } catch (error) {

    throw error;
  }
}

// 删除对话
export const deleteConversationPost = async (url) => {
  try {
    const response = await fetch(url, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'accessToken' : JSON.parse(localStorage.getItem("login-success-user")).accessToken,
      },
    });
    if (!response.ok) {
      throw new Error(`HTTP错误: 状态码 ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    throw error;
  }
}

// 上传图像
export const uploadImagePost = async (url, body) => {
  const formData = new FormData();
  const blob = dataURItoBlob(body);
  formData.append('file', blob, "image.jpg");
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Accept': '*/*',
        'accessToken' : JSON.parse(localStorage.getItem("login-success-user")).accessToken,
      },
      body: formData,
    });
    if (!response.ok) {
      throw new Error(`HTTP错误: 状态码 ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    throw error;
  }
}

const dataURItoBlob = (dataURI) => {
    const byteString = atob(dataURI.split(',')[1]);
    const mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];
    const ab = new ArrayBuffer(byteString.length);
    const ia = new Uint8Array(ab);
    for (let i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
    }
    return new Blob([ab], { type: mimeString });
}


