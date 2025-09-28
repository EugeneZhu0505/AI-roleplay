import "./styles/roleplayList.css"
import { useState, useEffect, useCallback } from 'react';
import RoleplayListSlide from './roleplayCardItemSlide';
import AudioSlide from './audioSlide';
import { getApiNotBody } from './utils/api';
import React from "react";
import RoleplaySearchItem from './roleplaySearchItem';


const RoleplayList = React.memo((props) => {
    const [username, setUsername] = useState('');
    const [image, setImage] = useState('');
    const [roleplayList, setRoleplayList] = useState([]);
    const [isLoadingRecommend, setIsLoadingRecommend] = useState(true); 
    const [isLoadingCategory, setIsLoadingCategory] = useState(true);
    const [isLoadingAudio, setIsLoadingAudio] = useState(true);
    const [categoryRoleplayList, setCategoryRolePlayList] = useState([]);
    const [audioList, setAudioList] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [category, setCategory] = useState(0);

    useEffect(() => {
        const loginSuccessUser = localStorage.getItem("login-success-user");
        if (loginSuccessUser) {
            const user = JSON.parse(loginSuccessUser);
            setUsername(user.username);
            setImage(user.avatarUrl);
        }

        const handleGetRoleplayListResonse = async () => {
            try{
                setIsLoadingRecommend(true); 
                const roleplayList = await getApiNotBody(`${process.env.REACT_APP_API_BASE_URL}/api/characters`);
                setRoleplayList(roleplayList.data);
                setIsLoadingRecommend(false); 
            }catch (error){
                console.error('获取角色列表失败:', error);
            }
        }
        handleGetRoleplayListResonse();
        handleGetCategoryRoleplayResponse(0);


        const handleGetCharacterAudio = async () =>{
            try{
                setIsLoadingAudio(true);
                const audioList = await getApiNotBody(`${process.env.REACT_APP_API_BASE_URL}/api/conversations/characters/opening-audios`);
                setAudioList(Object.values(audioList.data));
                setIsLoadingAudio(false);
            }catch (error){
                console.error('获取角色开场白失败:', error);
            }
        }
        handleGetCharacterAudio();
    }, []);

    const handleGetCategoryRoleplayResponse = useCallback(async (category) =>{
        setCategory(category);
        try{
            setIsLoadingCategory(true); 
            const roleplayList = await getApiNotBody(`${process.env.REACT_APP_API_BASE_URL}/api/characters/category/${category}`);
            setCategoryRolePlayList(roleplayList.data);
            setIsLoadingCategory(false); 
        }catch (error){
            console.error('获取角色列表失败:', error);
        }
    })

    const handleOnSearch = useCallback(async (e) => {
        if (e.key === 'Enter') {
            if (searchTerm.trim() !== '') {
                const searchCharacter = await getApiNotBody(`${process.env.REACT_APP_API_BASE_URL}/api/characters/search?name=${searchTerm}`);
                setSearchResults(searchCharacter.data);
            }
        }
    }, [roleplayList, searchTerm]);


    return(
        <div className="roleplayList-container">

            {/** 页面头*/}
            <div className="roleplayList-header">
                <div className="roleplayList-header-welcome-comtainer">
                    <div className="roleplayList-header-welcome">
                        欢迎回来,
                    </div>
                    <div className="roleplayList-header-user">
                        <img className="roleplayList-header-user-img" src={image==="" ? image : require("../imgs/place_charac_image.png")}/>
                        <div className="roleplayList-header-user-name">
                            {username}
                        </div>
                    </div>
                </div>

                <div className="roleplayList-header-search-container">
                    <div className="roleplayList-header-search-line">
                        <img className="roleplayList-header-search-icon" src={require("../imgs/search-icon.png")}/>
                        <input className="roleplayList-header-search" type="text" placeholder="搜索" onChange={(e) => setSearchTerm(e.target.value)} onKeyDown={handleOnSearch}/>
                    </div>
                    { searchTerm && (
                    <div className="search-results-container">
                        {searchResults.map(result => (
                            <RoleplaySearchItem key={result.id} searchItem={result}/>
                        ))}
                    </div>
                    )}
                </div>
            </div>
                

            {/** 热门推荐角色列表 */}
            <div className="roleplayList-recommend-container">
                <div className="roleplayList-recommend-header">
                    热门推荐
                </div>

                <div className="roleplayList-recommend-list-container">
                    {isLoadingRecommend ?
                    (<p>角色列表加载中...</p>):
                    <RoleplayListSlide  roleplayList={roleplayList}/>
                    }
            
                </div>
            </div>


            {/** 分类列表 */}
            <div className="roleplayList-category-container">
                <div className="roleplayList-category-button-container">
                    <button className={`roleplayList-category-button ${category === 0 ? 'active' : ''}`} key="0" onClick={() => handleGetCategoryRoleplayResponse(0)}>动漫</button>
                    <button className={`roleplayList-category-button ${category === 1 ? 'active' : ''}`} key="1" onClick={() => handleGetCategoryRoleplayResponse(1)}>影视</button>
                    <button className={`roleplayList-category-button ${category === 2 ? 'active' : ''}`} key="2" onClick={() => handleGetCategoryRoleplayResponse(2)}>历史</button>
                    <button className={`roleplayList-category-button ${category === 3 ? 'active' : ''}`} key="3" onClick={() => handleGetCategoryRoleplayResponse(3)}>科普</button>
                </div>

                <div className="roleplayList-category-list-container">
                        <div className="roleplayList-category-list-container">
                        {isLoadingCategory ?
                        (<p>角色列表加载中...</p>):
                        <RoleplayListSlide  handleRoleplayCardClick={props.handleRoleplayCardClick} roleplayList={categoryRoleplayList}/>
                        }
                    </div>
                </div>
            </div>


            {/** 语音分类 */}
            <div className="rolepalyList-voice-container">
                <div className="rolepalyList-voice-header">
                    语音
                </div>
                <div className="rolepalyList-voice-list-container">
                    {isLoadingAudio ?
                    (<p>角色列表加载中...</p>):
                    <AudioSlide  audioList={audioList}/>
                    }
                </div>
   
            </div>

        </div>
    )
})

export default RoleplayList;