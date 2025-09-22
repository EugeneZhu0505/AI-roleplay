import './App.css';
import { Route, Routes } from 'react-router-dom';
import LoginPage from './components/loginPage';
import HomePage from './components/homePage';

function App() {
  return (
    <div className="App">
      <Routes>
        {/* 登录页面路由 */}
        <Route path="/" element={<LoginPage />} />
        
        {/* 主页路由 */}
        <Route path="/home" element={<HomePage />} />
      </Routes>
    </div>
  );
}

export default App;
