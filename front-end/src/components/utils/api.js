/**
 * 向服务器发送请求的通用函数（React中使用）
 * @param {string} url - 请求地址
 * @param {string} method - 请求方法（GET/POST/PUT/DELETE等）
 * @param {object} [data] - 请求参数（GET会拼到URL，POST放在body）
 * @param {object} [headers] - 自定义请求头
 * @returns {Promise} - 服务器响应数据
 */
export const request = async (url, method, data = null, headers = {}) => {
  // 默认请求头
  const defaultHeaders = {
    'Content-Type': 'application/json',
    ...headers
  };

  // 配置请求选项
  const options = {
    method,
    headers: defaultHeaders
  };

  // 处理URL和参数
  let requestUrl = url;
  if (method.toUpperCase() === 'GET' && data) {
    // GET请求：参数拼接到URL
    const params = new URLSearchParams(data);
    requestUrl = `${url}?${params.toString()}`;
  } else if (data) {
    // 非GET请求：参数放入请求体
    options.body = JSON.stringify(data);
  }

  try {
    // 发送请求
    const response = await fetch(requestUrl, options);
    
    // 检查响应状态
    if (!response.ok) {
      throw new Error(`HTTP错误：状态码 ${response.status}`);
    }
    
    // 解析响应数据（支持JSON和文本）
    const contentType = response.headers.get('content-type');
    return contentType?.includes('application/json') 
      ? await response.json() 
      : await response.text();
  } catch (error) {
    console.error('请求失败：', error.message);
    throw error; // 抛出错误让调用方处理
  }
};


export const postRequest = async (url, data) => {
  // 默认请求头，支持JSON格式
  const defaultHeaders = {
    'Content-Type': 'application/json',
    'accessToken' : 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlcjExMTEiLCJ1c2VySWQiOjUsImlhdCI6MTc1ODYxOTEwMywiZXhwIjoxNzU4NzA1NTAzfQ.UTr99fJvT7JQENTHFd3eecx5JBgmCU4dlsZrIfWutJY',
  };

  try {
    // 发送POST请求
    const response = await fetch(url,{
      method: 'POST',
      headers: defaultHeaders,
      body: JSON.stringify(data) // 将数据转换为JSON字符串
    });

    // 检查响应状态
    if (!response.ok) {
      throw new Error(`请求失败：${response.status} ${response.statusText}`);
    }

    // 解析响应数据
    const contentType = response.headers.get('content-type');
    return contentType?.includes('application/json')
      ? await response.json()
      : await response.text();

  } catch (error) {
    console.error('POST请求出错：', error.message);
    throw error; // 抛出错误让调用方处理
  }
};


export const createConversation = async (userId, characterId, title = '') => {

  const defaultHeaders = {
    'Content-Type': 'application/json',
    'accessToken' : 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlcjExMTEiLCJ1c2VySWQiOjUsImlhdCI6MTc1ODYxOTEwMywiZXhwIjoxNzU4NzA1NTAzfQ.UTr99fJvT7JQENTHFd3eecx5JBgmCU4dlsZrIfWutJY',
  };

  try {
    // 发送GET请求（因为参数是查询参数，接口实际是POST但参数在URL上）
    const response = await request(
      `http://122.205.70.147:8080/api/conversations?userId=${userId}&characterId=${characterId}&title=${title}`, 
      'POST', 
      defaultHeaders,
    );
    return response;
  } catch (error) {
    throw error;
  }
};


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
