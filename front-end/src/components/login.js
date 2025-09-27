import { Modal} from 'antd';
import { useState, useEffect, useRef } from 'react';
import './styles/login.css';
import { LoginPost } from './utils/api';

const Login = ({ visible, onClose, successLogin }) => {

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const passwordRef = useRef(null);


    const handleLoginResponse = async (fromData) => {
        try{
            const result = await LoginPost(`${process.env.REACT_APP_API_BASE_URL}/api/auth/login`, fromData)
            if (result) {
                localStorage.setItem("login-success-user", JSON.stringify(
                    {
                        "username": result.data.username,
                        "avatarUrl": result.data.avatarUrl,
                        "userId": result.data.userId,
                        "password": passwordRef.current,
                        "accessToken": result.data.accessToken,
                    }));
                successLogin(true);
                onClose();
            }
        } catch (error) {
            alert('登录请求出错：' + error.message);
        }
    } 


    const handleOk = async () => {
        // 验证表单数据
        if (!username || !password) {
            alert('请填写完整信息');
            return;
        }

        const fromData = {
            "username": username,
            "password": password,
        };
        passwordRef.current = password;
        await handleLoginResponse(fromData);

    }

    useEffect(() => {
        const loginSuccessUser = localStorage.getItem("login-success-user");
        if (loginSuccessUser) {
            const user = JSON.parse(loginSuccessUser);
            const formData = {
                "username": user.username,
                "password": user.password,
            };
            handleLoginResponse(formData).then()
        }
    }, []);

    const handleCancel = () => {
        onClose();
    }

    const handleChange = (e) => {
        if (e.target.name === 'username') {
            setUsername(e.target.value);
        } else if (e.target.name === 'password') {
            setPassword(e.target.value);
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
            </form>
        </Modal>
    );
};

export default Login;