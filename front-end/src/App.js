import './App.css';
import { Route, Routes } from 'react-router-dom';
import LoginPage from './components/loginPage';
import HomePage from './components/homePage';
import RoleplayChat from './components/roleplayChat';
import CreatePage from './components/createPage';
import RegisterPage from './components/register';
import { Navigate } from 'react-router-dom';
import { useState } from 'react';


function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  // 模拟登录成功后的更新状态函数
    const handleLoginSuccess = () => {
        setIsLoggedIn(true);
    };

  return (
    <div className="App">
      <Routes>
        {/* 登录页面路由 */}
        <Route path="/" element={<LoginPage onLoginSuccess={handleLoginSuccess} />} />
        
        {/* 主页路由 */}
        <Route path="/home" element={isLoggedIn ? <HomePage /> : <Navigate to="/" replace />} />

        {/* 角色对话页面路由 */}
        <Route path="/roleplay" element={isLoggedIn ? <RoleplayChat /> : <Navigate to="/" replace />} />
        
        {/* 创建页面路由 */}
        <Route path="/create" element={isLoggedIn ? <CreatePage /> : <Navigate to="/" replace />} />
        
        {/* 注册页面路由 */}
        <Route path="/register" element={<RegisterPage />} />


        {/* 捕获所有不存在的路由 */}
        <Route path="*" element={<Navigate to="/" replace />} />
        
      </Routes>
    </div>
  );
}

export default App;
