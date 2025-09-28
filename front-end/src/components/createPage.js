import React, { useState, useRef, useEffect } from 'react';

const CreatePage = () => {
  // 表单状态管理
  const [formData, setFormData] = useState({
    name: '',
    avatarUrl: '',
    description: '',
    personality: '',
    backgroundStory: '',
    tags: '',
    systemPrompt: '',
  });
  
  // 预览模式状态
  const [previewMode, setPreviewMode] = useState(false);
  // 表单验证状态
  const [errors, setErrors] = useState({});
  // 上传图片状态
  const [imagePreview, setImagePreview] = useState('');
  const fileInputRef = useRef(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  // 处理表单输入变化
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: null }));
    }
  };

  // 处理头像上传
  const handleImageUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result);
        setFormData(prev => ({ ...prev, avatar: reader.result }));
      };
      reader.readAsDataURL(file);
    }
  };

  // 触发文件上传
  const triggerFileUpload = () => {
    fileInputRef.current.click();
  };

  // 移除头像
  const removeImage = () => {
    setImagePreview('');
    setFormData(prev => ({ ...prev, avatar: '' }));
    fileInputRef.current.value = '';
  };

  // 表单验证
  const validateForm = () => {
    const newErrors = {};
    if (!formData.name.trim()) {
      newErrors.name = '角色名称不能为空';
    } else if (formData.name.length > 30) {
      newErrors.name = '角色名称不能超过30个字符';
    }
    if (!formData.description.trim()) {
      newErrors.description = '角色描述不能为空';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 处理表单提交
  const handleSubmit = (e) => {
    e.preventDefault();
    if (validateForm()) {
      setIsSubmitting(true);
      setTimeout(() => {
        setIsSubmitting(false);
        setSubmitSuccess(true);
        setTimeout(() => {
          setSubmitSuccess(false);
        }, 3000);
        console.log('角色创建成功:', formData);
      }, 1500);
    }
  };

  // 切换预览模式
  const togglePreviewMode = () => {
    setPreviewMode(!previewMode);
  };

  // 成功提示动画效果
  useEffect(() => {
    if (submitSuccess) {
      const successEl = document.getElementById('submit-success');
      if (successEl) {
        successEl.classList.add('show');
        setTimeout(() => {
          successEl.classList.remove('show');
        }, 3000);
      }
    }
  }, [submitSuccess]);

  return (
    <div style={styles.container}>
      {/* 顶部导航 */}
      <header style={styles.header}>
        <div style={styles.headerInner}>
          <h1 style={styles.headerTitle}>角色创建工具</h1>
          <button 
            onClick={togglePreviewMode}
            style={{...styles.button, ...styles.smallButton}}
          >
            {previewMode ? '编辑模式' : '预览'}
          </button>
        </div>
      </header>

      {/* 主内容区 */}
      <main style={styles.main}>
        {/* 页面标题 */}
        <div style={styles.pageHeader}>
          <h2 style={styles.pageTitle}>创建你的角色</h2>
          <p style={styles.pageSubtitle}>填写详细信息，塑造一个生动有趣的角色形象</p>
        </div>

        <div style={styles.grid}>
          {/* 左侧：角色预览 */}
          <div style={styles.previewCol}>
            <div style={styles.previewCard}>
              <h3 style={styles.previewTitle}>角色预览</h3>
              
              {/* 头像预览 */}
              <div style={styles.avatarContainer}>
                {imagePreview ? (
                  <img 
                    src={imagePreview} 
                    alt="角色头像预览" 
                    style={styles.avatarImg}
                  />
                ) : (
                  <div style={styles.avatarPlaceholder}>
                    <span>头像</span>
                  </div>
                )}
                <div style={styles.avatarOverlay}>
                  <button 
                    onClick={triggerFileUpload}
                    style={styles.avatarActionBtn}
                    title="上传头像"
                  >
                    上传
                  </button>
                  {imagePreview && (
                    <button 
                      onClick={removeImage}
                      style={{...styles.avatarActionBtn, ...styles.removeBtn}}
                      title="移除头像"
                    >
                      移除
                    </button>
                  )}
                </div>
                <input 
                  type="file" 
                  ref={fileInputRef}
                  onChange={handleImageUpload}
                  accept="image/*"
                  style={styles.fileInput}
                />
              </div>
              
              {/* 角色信息预览 */}
              <div style={styles.infoPreview}>
                <h4 style={styles.infoName}>{formData.name || '未命名角色'}</h4>
                {formData.title && (
                  <p style={styles.infoTitle}>{formData.title}</p>
                )}
              </div>
              
              
              <div style={styles.descriptionPreview}>
                <h5 style={styles.descriptionTitle}>角色简介</h5>
                <p style={styles.descriptionText}>{formData.description || '请填写角色描述...'}</p>
              </div>
            </div>
          </div>

          {/* 右侧：表单区域 */}
          <div style={{...styles.formCol, ...(previewMode ? styles.disabledOverlay : {})}}>
            <form onSubmit={handleSubmit} style={styles.formCard}>
              {/* 基本信息部分 */}
              <div style={styles.formSection}>
                <h3 style={styles.sectionTitle}>基本信息</h3>
                
                <div style={styles.formGrid}>
                  {/* 角色名称 */}
                  <div style={styles.formGroup}>
                    <label htmlFor="name" style={styles.formLabel}>
                      角色名称 <span style={styles.required}>*</span>
                    </label>
                    <input
                      type="text"
                      id="name"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                      placeholder="输入角色的名称"
                      style={{...styles.formInput, ...(errors.name ? styles.errorInput : {})}}
                    />
                    {errors.name && (
                      <p style={styles.errorText}>{errors.name}</p>
                    )}
                  </div>
                  
                </div>
              </div>
              
              {/* 角色描述部分 */}
              <div style={styles.formSection}>
                <h3 style={styles.sectionTitle}>角色描述</h3>
                
                <div style={styles.formGroup}>
                  <label htmlFor="description" style={styles.formLabel}>
                    角色背景与故事 <span style={styles.required}>*</span>
                  </label>
                  <textarea
                    id="description"
                    name="description"
                    value={formData.description}
                    onChange={handleInputChange}
                    placeholder="描述这个角色的背景故事、经历、目标和动机..."
                    rows={5}
                    style={{...styles.formTextarea, ...(errors.description ? styles.errorInput : {})}}
                  />
                  {errors.description && (
                    <p style={styles.errorText}>{errors.description}</p>
                  )}
                  <p style={styles.hintText}>详细的背景故事有助于让角色更加丰满（至少20字）</p>
                </div>
              </div>
              
              {/* 角色性格部分 */}
              <div style={styles.formSection}>
                <h3 style={styles.sectionTitle}>性格特质</h3>
                
                <div style={styles.formGroup}>
                  <label htmlFor="personality" style={styles.formLabel}>性格特点与行为方式</label>
                  <textarea
                    id="personality"
                    name="personality"
                    value={formData.personality}
                    onChange={handleInputChange}
                    placeholder="描述这个角色的性格特点、说话方式、行为习惯等..."
                    rows={4}
                    style={styles.formTextarea}
                  />
                  <p style={styles.hintText}>例如：性格开朗，喜欢开玩笑，说话带点地方口音...</p>
                </div>
              </div>
              
              {/* 标签部分 */}
              <div style={styles.formSection}>
                <h3 style={styles.sectionTitle}>标签</h3>
                
                <div style={styles.formGroup}>
                  <label htmlFor="tags" style={styles.formLabel}>角色标签</label>
                  <input
                    type="text"
                    id="tags"
                    name="tags"
                    value={formData.tags}
                    onChange={handleInputChange}
                    placeholder="用逗号分隔标签，例如：勇敢,智慧,乐观"
                    style={styles.formInput}
                  />
                  <p style={styles.hintText}>标签有助于更好地分类和搜索角色（最多5个标签）</p>
                </div>
              </div>
              
              {/* 提交按钮 */}
              <div style={styles.submitGroup}>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  style={{...styles.button, ...(isSubmitting ? styles.loadingBtn : styles.primaryBtn)}}
                >
                  {isSubmitting ? (
                    <>
                      <svg className="animate-spin" style={styles.spinner} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      保存中...
                    </>
                  ) : (
                    <>
                      保存角色
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      </main>
      
      {/* 页脚 */}
      <footer style={styles.footer}>
        <p style={styles.footerText}>角色创建工具 © {new Date().getFullYear()}</p>
      </footer>
      
      {/* 提交成功提示 */}
      {submitSuccess && (
        <div id="submit-success" style={styles.successToast}>
          <span>角色创建成功！</span>
        </div>
      )}
    </div>
  );
};

// 内联样式定义
const styles = {
  container: {
    minHeight: '100vh',
    background: 'linear-gradient(to bottom, #f9fafb, #f3f4f6)',
    color: '#1f2937',
    fontFamily: 'system-ui, -apple-system, BlinkMacSystemFont, sans-serif',
  },
  header: {
    position: 'sticky',
    top: 0,
    zIndex: 50,
    background: 'rgba(255, 255, 255, 0.8)',
    backdropFilter: 'blur(8px)',
    borderBottom: '1px solid #e5e7eb',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.05)',
  },
  headerInner: {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '0.75rem 1rem',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: '1.25rem',
    fontWeight: 'bold',
    color: '#4f46e5',
  },
  button: {
    padding: '0.5rem 1rem',
    borderRadius: '0.375rem',
    border: 'none',
    cursor: 'pointer',
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.5rem',
    transition: 'all 0.2s ease',
  },
  smallButton: {
    fontSize: '0.875rem',
    background: '#f3f4f6',
    color: '#4b5563',
  },
  smallButtonHover: {
    background: '#e5e7eb',
  },
  main: {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '2rem 1rem',
  },
  pageHeader: {
    textAlign: 'center',
    marginBottom: '2.5rem',
  },
  pageTitle: {
    fontSize: 'clamp(1.8rem, 4vw, 2.5rem)',
    fontWeight: 'bold',
    marginBottom: '0.5rem',
  },
  pageSubtitle: {
    color: '#6b7280',
    maxWidth: '32rem',
    margin: '0 auto',
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: '1fr',
    gap: '2.5rem',
  },
  previewCol: {
    [window.innerWidth >= 1024 && 'gridColumn']: 'span 1',
  },
  previewCard: {
    background: 'white',
    borderRadius: '0.75rem',
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.05)',
    padding: '1.5rem',
    position: 'sticky',
    top: '3rem',
  },
  previewTitle: {
    fontSize: '1.125rem',
    fontWeight: 'semibold',
    marginBottom: '1.5rem',
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
  },
  avatarContainer: {
    width: '120px',
    height: '120px',
    borderRadius: '50%',
    overflow: 'hidden',
    margin: '0 auto 1.5rem',
    border: '4px solid #e0e7ff',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
    position: 'relative',
  },
  avatarImg: {
    width: '100%',
    height: '100%',
    objectFit: 'cover',
    transition: 'transform 0.3s ease',
  },
  avatarImgHover: {
    transform: 'scale(1.05)',
  },
  avatarPlaceholder: {
    width: '100%',
    height: '100%',
    background: '#f3f4f6',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#9ca3af',
  },
  avatarOverlay: {
    position: 'absolute',
    inset: 0,
    background: 'rgba(0, 0, 0, 0.4)',
    opacity: 0,
    transition: 'opacity 0.3s ease',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '0.5rem',
  },
  avatarContainerHover: {
    '&:hover .avatarOverlay': {
      opacity: 1,
    },
  },
  avatarActionBtn: {
    background: 'rgba(255, 255, 255, 0.9)',
    color: '#4b5563',
    borderRadius: '50%',
    width: '2.5rem',
    height: '2.5rem',
    border: 'none',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  removeBtn: {
    background: 'rgba(255, 255, 255, 0.9)',
    color: '#ef4444',
  },
  fileInput: {
    display: 'none',
  },
  infoPreview: {
    textAlign: 'center',
    marginBottom: '1.5rem',
  },
  infoName: {
    fontSize: '1.25rem',
    fontWeight: 'bold',
    marginBottom: '0.25rem',
  },
  infoTitle: {
    fontSize: '0.875rem',
    color: '#4f46e5',
    fontStyle: 'italic',
  },
  tagsContainer: {
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'center',
    gap: '0.5rem',
    marginBottom: '1.5rem',
  },
  tag: {
    padding: '0.25rem 0.5rem',
    borderRadius: '9999px',
    fontSize: '0.75rem',
    background: '#f3f4f6',
    color: '#4b5563',
  },
  tagHighlight: {
    padding: '0.25rem 0.5rem',
    borderRadius: '9999px',
    fontSize: '0.75rem',
    background: '#e0e7ff',
    color: '#4f46e5',
  },
  descriptionPreview: {
    borderTop: '1px solid #e5e7eb',
    paddingTop: '1rem',
  },
  descriptionTitle: {
    fontSize: '0.875rem',
    fontWeight: 'semibold',
    marginBottom: '0.5rem',
    color: '#6b7280',
  },
  descriptionText: {
    fontSize: '0.875rem',
    color: '#6b7280',
    lineHeight: '1.5',
    display: '-webkit-box',
    WebkitLineClamp: 3,
    WebkitBoxOrient: 'vertical',
    overflow: 'hidden',
  },
  formCol: {
    [window.innerWidth >= 1024 && 'gridColumn']: 'span 2',
    transition: 'all 0.3s ease',
  },
  disabledOverlay: {
    opacity: 0.5,
    pointerEvents: 'none',
  },
  formCard: {
    background: 'white',
    borderRadius: '0.75rem',
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.05)',
    padding: '2rem',
  },
  formSection: {
    marginBottom: '2rem',
  },
  sectionTitle: {
    fontSize: '1.25rem',
    fontWeight: 'semibold',
    marginBottom: '1.25rem',
    paddingBottom: '0.75rem',
    borderBottom: '1px solid #e5e7eb',
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
  },
  formGrid: {
    display: 'grid',
    gridTemplateColumns: '1fr',
    gap: '1.5rem',
  },
  [window.innerWidth >= 768 && 'formGrid']: {
    gridTemplateColumns: '1fr 1fr',
  },
  formGroup: {
    marginBottom: '1rem',
  },
  formLabel: {
    display: 'block',
    fontSize: '0.875rem',
    fontWeight: 'medium',
    marginBottom: '0.5rem',
  },
  required: {
    color: '#ef4444',
  },
  formInput: {
    width: '100%',
    padding: '0.75rem 1rem',
    borderRadius: '0.375rem',
    border: '1px solid #d1d5db',
    boxSizing: 'border-box',
    transition: 'all 0.2s ease',
  },
  formInputFocus: {
    borderColor: '#4f46e5',
    boxShadow: '0 0 0 3px rgba(79, 70, 229, 0.15)',
  },
  errorInput: {
    borderColor: '#ef4444',
    boxShadow: '0 0 0 3px rgba(239, 68, 68, 0.15)',
  },
  formTextarea: {
    width: '100%',
    padding: '0.75rem 1rem',
    borderRadius: '0.375rem',
    border: '1px solid #d1d5db',
    boxSizing: 'border-box',
    resize: 'none',
    transition: 'all 0.2s ease',
  },
  radioGroup: {
    display: 'flex',
    gap: '1.5rem',
    marginTop: '0.5rem',
  },
  radioLabel: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    cursor: 'pointer',
  },
  radioInput: {
    width: '1rem',
    height: '1rem',
    accentColor: '#4f46e5',
  },
  radioText: {
    fontSize: '0.875rem',
  },
  hintText: {
    fontSize: '0.75rem',
    color: '#6b7280',
    marginTop: '0.5rem',
  },
  errorText: {
    fontSize: '0.75rem',
    color: '#ef4444',
    marginTop: '0.25rem',
    display: 'flex',
    alignItems: 'center',
    gap: '0.25rem',
  },
  submitGroup: {
    display: 'flex',
    justifyContent: 'flex-end',
    marginTop: '1.5rem',
    borderTop: '1px solid #e5e7eb',
    paddingTop: '1.5rem',
  },
  primaryBtn: {
    background: '#4f46e5',
    color: 'white',
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
  },
  primaryBtnHover: {
    background: '#4338ca',
    transform: 'translateY(-1px)',
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.15)',
  },
  loadingBtn: {
    background: '#a5b4fc',
    cursor: 'not-allowed',
  },
  spinner: {
    width: '1.5rem',
    height: '1.5rem',
  },
  footer: {
    background: 'white',
    borderTop: '1px solid #e5e7eb',
    padding: '1.5rem 1rem',
    textAlign: 'center',
    marginTop: '3rem',
  },
  footerText: {
    color: '#6b7280',
    fontSize: '0.875rem',
  },
  successToast: {
    position: 'fixed',
    bottom: '2rem',
    right: '2rem',
    background: '#10b981',
    color: 'white',
    padding: '0.75rem 1.25rem',
    borderRadius: '0.375rem',
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    animation: 'slideUp 0.3s ease forwards',
  },
  '@keyframes slideUp': {
    '0%': { transform: 'translateY(20px)', opacity: 0 },
    '100%': { transform: 'translateY(0)', opacity: 1 },
  },
};

export default CreatePage;