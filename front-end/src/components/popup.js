import './styles/popup.css';

const Popup = ({ isOpen, onClose, content }) => {
  if (!isOpen) return null;

  return (
    <>
      {/* 背景遮罩 */}
      <div className="popup-backdrop" onClick={onClose} />
      <div className="popup-overlay">
        <div className="popup-header">
          角色详情
        </div>
        <div className="popup-content">
          <span className="popup-close" onClick={onClose}>
            <img src={require("../imgs/close.png")} className='close'/>
          </span>
          <div className="popup-role-cover">
            <img src={content.avatarUrl} alt={content.name} />
          </div>
          <div className="popup-role-name">
            {content.name}
          </div>
          <div className="popup-role-description">
            {content.description}
          </div>
          <div className="popup-role-personality">
            {content.personality}
          </div>
        </div>
      </div>
    </>
  );
};

export default Popup;