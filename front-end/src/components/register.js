import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { RegisterPost } from './utils/api';

const RegisterPage = () => {
  // 表单状态管理
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    confirmPassword: ''
  });

  const navigate = useNavigate();
  
  // 头像相关状态
  const [avatar, setAvatar] = useState(''); // 存储头像Base64
  const [avatarPreview, setAvatarPreview] = useState(''); // 头像预览图
  const fileInputRef = useRef(null); // 文件输入框引用
  const [avatarUrl, setAvatarUrl] = useState(''); // 头像URL
  
  // 错误信息状态
  const [errors, setErrors] = useState({});
  
  // 注册状态
  const [isRegistering, setIsRegistering] = useState(false);
  const [registerSuccess, setRegisterSuccess] = useState(false);

  // 处理输入变化
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    
    // 清除对应字段的错误
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: null }));
    }
  };

  // 处理头像上传
  const handleAvatarUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      // 验证文件类型和大小
      const fileType = file.type;
      const fileSize = file.size / 1024 / 1024; // 转换为MB
      
      if (!fileType.startsWith('image/')) {
        setErrors(prev => ({ ...prev, avatar: '请上传图片格式的文件' }));
        return;
      }
      
      if (fileSize > 5) {
        setErrors(prev => ({ ...prev, avatar: '图片大小不能超过5MB' }));
        return;
      }
      
      // 清除头像错误
      if (errors.avatar) {
        setErrors(prev => ({ ...prev, avatar: null }));
      }
      
      // 读取图片并生成预览
      const reader = new FileReader();
      reader.onloadend = () => {
        setAvatar(reader.result); // 存储Base64用于提交
        setAvatarPreview(reader.result); // 显示预览
      };
      reader.readAsDataURL(file);

      const tempUrl = URL.createObjectURL(file);
      setAvatarUrl(tempUrl);
    }
  };

  // 触发文件选择框
  const triggerFileUpload = () => {
    if (!isRegistering) {
      fileInputRef.current.click();
    }
  };

  // 移除头像
  const removeAvatar = () => {
    setAvatar('');
    setAvatarPreview('');
    fileInputRef.current.value = '';
    if (errors.avatar) {
      setErrors(prev => ({ ...prev, avatar: null }));
    }
  };

  // 表单验证
  const validateForm = () => {
    const newErrors = {};
    
    // 用户名验证
    if (!formData.username.trim()) {
      newErrors.username = '用户名不能为空';
    } else if (formData.username.length < 3) {
      newErrors.username = '用户名至少需要3个字符';
    } else if (formData.username.length > 20) {
      newErrors.username = '用户名不能超过20个字符';
    }
    
    // 密码验证
    if (!formData.password) {
      newErrors.password = '密码不能为空';
    } else if (formData.password.length < 6) {
      newErrors.password = '密码至少需要6个字符';
    }
    
    // 确认密码验证
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = '请确认密码';
    } else if (formData.confirmPassword !== formData.password) {
      newErrors.confirmPassword = '两次输入的密码不一致';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 处理注册提交
  const handleRegister = async (e) => {
    e.preventDefault();
    
    if (validateForm()) {
      setIsRegistering(true);
      
      const submitData = {
        ...formData,
        avatar: avatar // 头像Base64（实际项目中可转成文件流或URL）
      };

      const registerUrl = `${process.env.REACT_APP_API_BASE_URL}/api/auth/register`;
      const response = await RegisterPost(registerUrl, submitData);
      if (response.code === 0) {
        setRegisterSuccess(true);
        setIsRegistering(false);
        localStorage.setItem("login-success-user", JSON.stringify(
          {
              "username": response.data.username,
              "avatarUrl": response.data.avatarUrl,
              "userId": response.data.userId,
              "password": formData.password,
              "accessToken": response.data.accessToken,
          }));
        navigate('/home');
      } else {
        setRegisterSuccess(false);
        setIsRegistering(false);
        setErrors({
          ...errors,
          username: response.msg || '注册失败，请稍后重试'
        });
      }
      
    }
  };

  return (
    <div style={styles.container}>
      {/* 注册卡片 */}
      <div style={styles.card}>
        <h1 style={styles.title}>创建账户</h1>
        <p style={styles.subtitle}>填写以下信息完成注册</p>
        
        {/* 头像上传区域 */}
        <div style={styles.avatarContainer}>
          {/* 头像预览 */}
          <div 
            style={styles.avatarPreview}
            onClick={triggerFileUpload}
            className="avatar-preview"
            title={avatarPreview ? '点击更换头像' : '点击上传头像'}
          >
            {avatarPreview ? (
              <img 
                src={avatarPreview} 
                alt="头像预览" 
                style={styles.avatarImg}
              />
            ) : (
              <div style={styles.avatarPlaceholder}>
                <span style={styles.avatarIcon}>👤</span>
              </div>
            )}
            
            {/* 移除头像按钮（仅在有头像时显示） */}
            {avatarPreview && (
              <button 
                style={styles.removeAvatarBtn}
                onClick={(e) => {
                  e.stopPropagation(); // 阻止冒泡，避免触发上传
                  removeAvatar();
                }}
                title="移除头像"
                disabled={isRegistering}
              >
                ×
              </button>
            )}
          </div>
          
          {/* 隐藏的文件输入框 */}
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleAvatarUpload}
            accept="image/*"
            style={styles.fileInput}
            disabled={isRegistering}
          />
          
          {/* 头像错误提示 */}
          {errors.avatar && (
            <p style={styles.errorText}>{errors.avatar}</p>
          )}
          
          {/* 上传提示文字 */}
          <p style={styles.avatarHint}>
            {avatarPreview ? '点击更换头像' : '点击上传头像'}（可选）
          </p>
        </div>
        
        <form onSubmit={handleRegister} style={styles.form}>
          {/* 用户名 */}
          <div style={styles.formGroup}>
            <label htmlFor="username" style={styles.label}>
              用户名 <span style={styles.required}>*</span>
            </label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
              placeholder="请输入用户名"
              style={{...styles.input, ...(errors.username ? styles.errorInput : {})}}
              disabled={isRegistering}
            />
            {errors.username && (
              <p style={styles.errorText}>{errors.username}</p>
            )}
          </div>
          
          {/* 密码 */}
          <div style={styles.formGroup}>
            <label htmlFor="password" style={styles.label}>
              密码 <span style={styles.required}>*</span>
            </label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleInputChange}
              placeholder="请输入密码"
              style={{...styles.input, ...(errors.password ? styles.errorInput : {})}}
              disabled={isRegistering}
            />
            {errors.password && (
              <p style={styles.errorText}>{errors.password}</p>
            )}
          </div>
          
          {/* 确认密码 */}
          <div style={styles.formGroup}>
            <label htmlFor="confirmPassword" style={styles.label}>
              确认密码 <span style={styles.required}>*</span>
            </label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleInputChange}
              placeholder="请再次输入密码"
              style={{...styles.input, ...(errors.confirmPassword ? styles.errorInput : {})}}
              disabled={isRegistering}
            />
            {errors.confirmPassword && (
              <p style={styles.errorText}>{errors.confirmPassword}</p>
            )}
          </div>
          
          {/* 注册按钮 */}
          <button
            type="submit"
            disabled={isRegistering}
            style={{...styles.button, ...(isRegistering ? styles.disabledButton : {})}}
          >
            {isRegistering ? (
              <>
                <svg style={styles.spinner} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle className="spinner-circle" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" strokeLinecap="round" strokeDasharray="50" strokeDashoffset="0"></circle>
                </svg>
                注册中...
              </>
            ) : '注册'}
          </button>
        </form>
      </div>
      
      {/* 注册成功提示 */}
      {registerSuccess && (
        <div style={styles.successToast}>
          <span>注册成功！</span>
        </div>
      )}
    </div>
  );
};

// 样式定义
const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)',
    padding: '20px',
    fontFamily: 'system-ui, -apple-system, sans-serif'
  },
  card: {
    background: 'white',
    borderRadius: '12px',
    boxShadow: '0 10px 30px rgba(0, 0, 0, 0.1)',
    width: '100%',
    maxWidth: '400px',
    padding: '40px 30px',
    boxSizing: 'border-box'
  },
  title: {
    fontSize: '24px',
    fontWeight: '700',
    color: '#333',
    margin: '0 0 8px 0',
    textAlign: 'center'
  },
  subtitle: {
    fontSize: '14px',
    color: '#666',
    margin: '0 0 20px 0',
    textAlign: 'center'
  },
  // 头像相关样式
  avatarContainer: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    marginBottom: '30px'
  },
  avatarPreview: {
    width: '100px',
    height: '100px',
    borderRadius: '50%',
    border: '2px dashed #ddd',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    cursor: 'pointer',
    position: 'relative',
    overflow: 'hidden',
    transition: 'all 0.3s ease'
  },
  avatarImg: {
    width: '100%',
    height: '100%',
    objectFit: 'cover'
  },
  avatarPlaceholder: {
    width: '100%',
    height: '100%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center'
  },
  avatarIcon: {
    fontSize: '32px',
    color: '#999'
  },
  removeAvatarBtn: {
    position: 'absolute',
    right: '5px',
    bottom: '5px',
    width: '24px',
    height: '24px',
    borderRadius: '50%',
    background: 'rgba(0, 0, 0, 0.6)',
    color: 'white',
    border: 'none',
    fontSize: '16px',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '0',
    transition: 'background 0.2s ease'
  },
  fileInput: {
    display: 'none'
  },
  avatarHint: {
    fontSize: '12px',
    color: '#666',
    marginTop: '8px',
    textAlign: 'center'
  },
  // 表单样式
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '15px'
  },
  formGroup: {
    marginBottom: '10px'
  },
  label: {
    display: 'block',
    fontSize: '14px',
    color: '#333',
    marginBottom: '8px',
    fontWeight: '500'
  },
  required: {
    color: '#e74c3c',
    fontSize: '14px'
  },
  input: {
    width: '100%',
    padding: '12px 15px',
    borderRadius: '8px',
    border: '1px solid #ddd',
    fontSize: '16px',
    boxSizing: 'border-box',
    transition: 'all 0.3s ease'
  },
  inputFocus: {
    borderColor: '#4a90e2',
    outline: 'none',
    boxShadow: '0 0 0 3px rgba(74, 144, 226, 0.2)'
  },
  errorInput: {
    borderColor: '#e74c3c',
    boxShadow: '0 0 0 3px rgba(231, 76, 60, 0.1)'
  },
  errorText: {
    color: '#e74c3c',
    fontSize: '12px',
    marginTop: '5px',
    display: 'flex',
    alignItems: 'center',
    gap: '4px',
    marginBottom: '0'
  },
  // 按钮样式
  button: {
    width: '100%',
    padding: '14px',
    borderRadius: '8px',
    border: 'none',
    backgroundColor: '#4a90e2',
    color: 'white',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer',
    transition: 'all 0.3s ease',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    gap: '8px'
  },
  buttonHover: {
    backgroundColor: '#3a7bc8',
    transform: 'translateY(-2px)',
    boxShadow: '0 4px 12px rgba(74, 144, 226, 0.3)'
  },
  disabledButton: {
    backgroundColor: '#a0c4f5',
    cursor: 'not-allowed',
    transform: 'none',
    boxShadow: 'none'
  },
  // 加载动画
  spinner: {
    width: '18px',
    height: '18px',
    animation: 'spin 1s linear infinite'
  },
  // 成功提示
  successToast: {
    position: 'fixed',
    top: '20px',
    left: '50%',
    transform: 'translateX(-50%)',
    backgroundColor: '#2ecc71',
    color: 'white',
    padding: '12px 24px',
    borderRadius: '8px',
    boxShadow: '0 4px 12px rgba(46, 204, 113, 0.3)',
    fontSize: '14px',
    fontWeight: '500',
    animation: 'slideDown 0.3s ease-out forwards'
  }
};

// 添加全局样式
const style = document.createElement('style');
style.textContent = `
  .spinner-circle {
    animation: spinner-dash 1.5s ease-in-out infinite;
    stroke-dasharray: 80px, 200px;
    stroke-dashoffset: 0;
  }
  
  @keyframes spinner-dash {
    0% {
      stroke-dasharray: 1px, 200px;
      stroke-dashoffset: 0;
    }
    50% {
      stroke-dasharray: 100px, 200px;
      stroke-dashoffset: -15px;
    }
    100% {
      stroke-dasharray: 100px, 200px;
      stroke-dashoffset: -125px;
    }
  }
  
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
  
  @keyframes slideDown {
    0% { transform: translate(-50%, -100%); }
    100% { transform: translate(-50%, 0); }
  }
  
  input:focus {
    border-color: #4a90e2;
    outline: none;
    box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.2);
  }
  
  button:not(:disabled):hover {
    background-color: #3a7bc8;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(74, 144, 226, 0.3);
  }
  
  .avatar-preview:hover {
    border-color: #4a90e2;
    background-color: rgba(74, 144, 226, 0.05);
  }
  
  .removeAvatarBtn:hover {
    background-color: rgba(231, 76, 60, 0.8);
  }
`;
document.head.appendChild(style);

export default RegisterPage;
