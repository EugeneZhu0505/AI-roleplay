import { Modal } from 'antd';

const Register = ({ visible, onClose, successLogin }) => {

       return (
        <Modal
        title="注册"
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
}

export default Register;
