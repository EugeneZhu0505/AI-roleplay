import '../App.css';
import './styles/loginContent.css';
import { useNavigate } from 'react-router-dom';

function LoginPage() {
  const navigate = useNavigate();
  
  const handleRegisterClick = () => {
    console.log('注册聊天');
  };

  const handleLoginClick = () => {
    console.log('登录');
    navigate('/home'); // 登录成功后跳转到主页
  };

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
        </div>
      </header>

      {/* 主要内容 */}
      <main className="login-content">
        {/* 动画图片容器 */}
        <div className="animated-gifs">
          <div className="gif-item">
            <video 
              src={require('./imgs/harrypotter.mp4')}
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
              src={require('./imgs/Einstein.mp4')}
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
              src={require('./imgs/libai.mp4')}
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
            <video 
              src="https://via.placeholder.com/150/ff9800/ffffff?text=沉浸式+体验" 
              alt="AI角色4" 
              width="100%" 
              height="100%" 
              autoPlay
              loop
              muted
              preload="auto"
            />
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