import React, { useState, useRef, useEffect } from 'react';
import './styles/pullUpMenu.css';


const skillOptions = [
    { characterId: 1, skill: ["模仿咒语生成","魔法决斗", "不使用技能"] },
    { characterId: 2, skill: ["冰雪魔法创造","冰雪城堡建造", "不使用技能"] },
    { characterId: 3, skill: ["科技发明设计","钢铁战甲升级", "不使用技能"] },
    { characterId: 4, skill: ["七十二变神通","如意金箍棒", "不使用技能"] },
    { characterId: 5, skill: ["橡胶能力应用","霸王色霸气", "不使用技能"] },
    { characterId: 6, skill: ["智谋策略制定","奇门遁甲", "不使用技能"] },
    { characterId: 7, skill: ["电气技能释放","十万伏特", "不使用技能"] },
    { characterId: 8, skill: ["人生智慧分享","跑步马拉松", "不使用技能"] },
    { characterId: 9, skill: ["未来道具帮助","时光机穿越", "不使用技能"] },
  ];

const PullUpMenu = ({ characterId, onSelect }) => {
  // 状态管理：控制选项菜单的显示与隐藏
  const [showOptions, setShowOptions] = useState(false);
  // 引用DOM元素
  const containerRef = useRef(null);
  const [options, setOptions] = useState([]);
  const [selectedSkill, setSelectedSkill] = useState("角色技能");



  // 点击选项的处理函数
  const handleOptionClick = (option) => {
    if (option === "不使用技能"){
        setSelectedSkill("角色技能");
    } else {
        setSelectedSkill(option);
    }
    onSelect(option);
      
    setShowOptions(false); // 选择后隐藏选项菜单
  };

  

    useEffect(() => {
        setSelectedSkill("角色技能");
        const options = skillOptions.find(item => item.characterId === characterId)?.skill || [];
        setOptions(options);
    }, [characterId]);
    
    // 点击外部区域关闭菜单
    useEffect(() => {
    
    const handleClickOutside = (event) => {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setShowOptions(false);
      }
    };
    
    // 监听全局点击事件
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      // 清理函数：移除事件监听
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);
  
  return (
    <div className="roleplay-skill-pull-up" ref={containerRef}>
      {/* 触发按钮 */}
      <p 
        className="roleplay-skill" 
        onClick={() => setShowOptions(!showOptions)}
      >
        {selectedSkill}
      </p>
      
      {/* 上拉选项菜单 */}
      <div className={`pull-up-options ${showOptions ? 'show' : ''}`}>
        {options.map((option, index) => (
          <div 
            key={index}
            className="pull-up-option"
            onClick={() => handleOptionClick(option)}
          >
            {option}
          </div>
        ))}
      </div>
    </div>
  );
};

export default PullUpMenu;
