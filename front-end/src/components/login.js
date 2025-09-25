import { Modal} from 'antd';
import { useState, useEffect } from 'react';
import './styles/login.css';
import { LoginPost } from './utils/api';

const Login = ({ visible, onClose, successLogin }) => {

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [captcha, setCaptcha] = useState('');
    const [generatedCaptcha,setGeneratedCaptcha] = useState('');

    const generateCaptcha = () => {
        const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        let captcha = '';
        for (let i = 0; i < 4; i++) {
            captcha += characters.charAt(Math.floor(Math.random() * characters.length));
        }
        setGeneratedCaptcha(captcha);
    };

    const handleOk = async () => {
        // 验证表单数据
        if (!username || !password || !captcha) {
            alert('请填写完整信息');
            return;
        }

        if (captcha === generatedCaptcha) {
            const fromData = {
                "username": username,
                "password": password,
            };
            
            // try{
            //     const result = await LoginPost('http://122.205.70.147:8080/api/auth/login', fromData)
            //     if (result) {
            //         localStorage.setItem('accessToken', result.data.accessToken);
            //         localStorage.setItem('userId', result.data.userId);
            //         localStorage.setItem('username', result.data.username);
            //         localStorage.setItem('image', result.data.avatarUrl);
            //         successLogin(true);
            //         onClose();
            //     }
            // } catch (error) {
            //     alert('登录请求出错：' + error.message);
            // }
            successLogin(true);
            
        } else {
            alert('验证码错误');
        }
    }

    useEffect(() => {
        generateCaptcha();
        setUsername(localStorage.getItem('username') || '');
        setPassword(localStorage.getItem('password') || '');
        setCaptcha('');
    }, []);

    const handleCancel = () => {
        onClose();
    }

    const handleChange = (e) => {
        if (e.target.name === 'username') {
            setUsername(e.target.value);
        } else if (e.target.name === 'password') {
            setPassword(e.target.value);
        } else if (e.target.name === 'captcha') {
            setCaptcha(e.target.value);
        }
    }
    
    return (
        <Modal
        title="登录"
        open={visible}
        onOk={handleOk}
        onCancel={handleCancel}
        className="login-modal-container"
        >
            <form>
                <div className="form-item">
                    <label className="form-label">账号:</label>
                    <input type="text" className="form-input" name="username" onChange={handleChange} />
                </div>
                <div className="form-item">
                    <label className="form-label">密码:</label>
                    <input type="password" className="form-input" name="password" onChange={handleChange} />
                </div>
                <div className="form-item">
                    <label className="form-label">验证码:</label>
                    <input type="text" className="form-input" name="captcha" onChange={handleChange} />
                    <span className="captcha-text">{generatedCaptcha}</span>
                    <button
                    type="button"   
                    className="refresh-captcha-button"
                    onClick={generateCaptcha}
                    >
                    刷新
                    </button>
                </div>
            </form>
        </Modal>
    );
};

export default Login;