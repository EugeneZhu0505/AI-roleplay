import RoleplayHistoryListItem from './roleplayHistoryListItem'
import './styles/roleplayHistoryList.css'
import {HistoryAICharacter} from './utils/historyAICharacter';
import { getApiNotBody } from './utils/api';
import { useContext, useEffect } from 'react';
import React from 'react';

const RoleplayHistoryList = React.memo(() => {  
    const {filteredCharacters, setChatHistoryCharacters} = useContext(HistoryAICharacter);
    const userId = JSON.parse(localStorage.getItem("login-success-user")).userId;

    useEffect(()=>{
        const handleGetHistoryRoleplayList = async() => {
            let url = `${process.env.REACT_APP_API_BASE_URL}/api/conversations?userId=${userId}`;
            const historyResponse = await getApiNotBody(url);
            const historyData = historyResponse.data;

            const promiseArray = historyData.map((item) => {
                url = `${process.env.REACT_APP_API_BASE_URL}/api/characters/${item.characterId}`
                return getApiNotBody(url)
                .then(characterResponse => {
                    const characterData = characterResponse.data;
                    return {
                        conversationId: item.id,
                        characterId: characterData.id,
                        cover: characterData.avatarUrl,
                        name: characterData.name,
                        description: characterData.description,
                        title: item.title,
                    }
                })
            });

            const newData = await Promise.all(promiseArray);
            setChatHistoryCharacters(newData);
        }

        handleGetHistoryRoleplayList();
    }, [])

    return(     
        <div className="roleplay-history-list-container">
            <div className="roleplay-history-list-title">
                历史对话角色
            </div>
            <div className="roleplay-history-list-item-container">
                {
                    filteredCharacters.map((roleplay) => {
                        return <RoleplayHistoryListItem roleplay={roleplay} />
                    })
                }
            </div>

        </div>
    )
})

export default RoleplayHistoryList