import './App.css';
import { Route, Routes } from 'react-router-dom';
import LoginPage from './components/loginPage';
import HomePage from './components/homePage';
import RoleplayChat from './components/roleplayChat';
import CreatePage from './components/createPage';
import RegisterPage from './components/register';



function App() {
  return (
    <div className="App">
      <Routes>
        {/* 登录页面路由 */}
        <Route path="/" element={<LoginPage />} />
        
        {/* 主页路由 */}
        <Route path="/home" element={<HomePage />} />

        {/* 角色对话页面路由 */}
        <Route path="/roleplay" element={<RoleplayChat />} />
        
        {/* 创建页面路由 */}
        <Route path="/create" element={<CreatePage />} />
        
        {/* 注册页面路由 */}
        <Route path="/register" element={<RegisterPage />} />
      </Routes>
    </div>
  );
}

export default App;
