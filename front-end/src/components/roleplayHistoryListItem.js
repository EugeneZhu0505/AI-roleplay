import './styles/roleplayHistoryListItem.css'
import { useContext, useState } from 'react';
import {SelectedAICharacter, HistoryAICharacter} from './utils/historyAICharacter';
import {deleteConversationPost} from './utils/api';

const RoleplayHistoryListItem = ({roleplay}) => {
    const {selectedRoleplay, setSelectedRoleplay, setIsChat} = useContext(SelectedAICharacter);
    const {chatHistoryCharacters, setChatHistoryCharacters} = useContext(HistoryAICharacter);
    const userId = JSON.parse(localStorage.getItem("login-success-user")).userId;

    const handleClickHistoryItem = () => {
        setSelectedRoleplay({
            conversationId: roleplay.conversationId,
            characterId: roleplay.characterId,
            cover: roleplay.cover,
            name: roleplay.name,
            description: roleplay.description,
            title: roleplay.title,
        })
        setIsChat(true);
    }

    const handleClickMoreFunc = () => {

        const deleteUrl = `${process.env.REACT_APP_API_BASE_URL}/api/conversations/${roleplay.conversationId}?userId=${userId}`;
        deleteConversationPost(deleteUrl).then(res => {
            setChatHistoryCharacters(chatHistoryCharacters.filter(item => item.conversationId !== roleplay.conversationId));
        }).catch(err => {
            alert(`删除失败, ${err.message}`);
        })
    }




    return(
        <div className={`roleplay-history-card-container ${selectedRoleplay.conversationId === roleplay.conversationId ? 'selected' : ''}`} key={roleplay.conversationId}>
            <div className="roleplay-history-list-item-content" onClick={handleClickHistoryItem}>
                <img src={roleplay.cover} className="roleplay-history-list-item-avatar" />
                <div className="roleplay-history-list-item-name">
                    {roleplay.name}
                </div>
            </div>
            <div className="roleplay-history-list-item-more-func-container">
                <img src={require("../imgs/more.png")} className="roleplay-history-list-item-more-func" onClick={handleClickMoreFunc}/>
            </div>
        </div>
    )
}

export default RoleplayHistoryListItem
