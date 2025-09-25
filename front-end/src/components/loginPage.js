import { useNavigate } from 'react-router-dom';
import {useState } from 'react';
import './styles/loginContent.css';
import Login from './login';

const LoginPage = () => {
  const [visible, setVisible] = useState(false);

  const navigate = useNavigate();
  
  const handleRegisterClick = () => {
    setVisible(true);
  };

  const handleLoginClick = () => {
    setVisible(true);
  };
  
  const handleCloseLogin = () => {
    setVisible(false);
  };

  const successLogin = (success) => {
    if (success) {
      navigate('/home');
    }
  }


  return (
    <div className="login-page">
      {/* 标题栏 */}
      <header className="header">
        <div className="logo">Roleplay.AI</div>
        <div className="header-buttons">
          <button className="btn btn-primary" onClick={handleRegisterClick}>
            注册聊天
          </button>
          <button className="btn btn-secondary" onClick={handleLoginClick}>
            登录
          </button>
          <Login visible={visible} onClose={handleCloseLogin} successLogin={successLogin} />
        </div>
      </header>

      {/* 主要内容 */}
      <main className="login-content">
        {/* 动画图片容器 */}
        <div className="animated-gifs">
          <div className="gif-item">
            <video 
              src={require('../imgs/harrypotter.mp4')}
              alt="AI角色1" 
              width="440" 
              height="100%"
              autoPlay
              loop
              muted
              preload="auto"
            />
          </div>
          <div className="gif-item">
            <video 
              src={require('../imgs/Einstein.mp4')}
              alt="AI角色2" 
              width="100%" 
              height="100%" 
              autoPlay
              loop
              muted
              preload="auto"
            />
          </div>
          <div className="gif-item">
            <video 
              src={require('../imgs/libai.mp4')}
              alt="AI角色3" 
              width="100%" 
              height="340px" 
              autoPlay
              loop
              muted
              preload="auto"
            />
          </div>
          <div className="gif-item">
            <div style={{width: '100%', height: '100%', backgroundColor: '#ff9800', display: 'flex', justifyContent: 'center', alignItems: 'center', color: 'white', fontSize: '16px', fontWeight: 'bold'}}>
              沉浸式体验
            </div>
          </div>
        </div>
        
        {/* 登录容器 */}
        <div className="login-container">
          <h1>欢迎来到 Roleplay.AI</h1>
          <p>与AI角色进行沉浸式对话体验</p>
        </div>
      </main>
    </div>
  );
}

export default LoginPage;