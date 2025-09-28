import "./styles/roleplayCardItem.css"
import { useContext } from 'react';
import {HistoryAICharacter, SelectedAICharacter} from './utils/historyAICharacter';
import React from "react";

const RoleplayCardItem = (props) => {

    const {setSelectedRoleplay, setIsChat} = useContext(SelectedAICharacter);

    const handleClick = () => {
        setSelectedRoleplay({
            characterId: props.roleplay.id,
            cover: props.roleplay.cover,
            name: props.roleplay.name,
            description: props.roleplay.desc,
            personality: props.roleplay.personality,
        })
        setIsChat(true);
    }


    return(
        <div className="roleplay-card-item-container" key={props.roleplay.id} onClick={handleClick}>
            <img src={props.roleplay.cover} className="roleplay-card-item-image" />

            <div className="roleplay-card-item-text-container">
                <div className="roleplay-card-item-text-name">
                    {props.roleplay.name}
                </div>
                <div className="roleplay-card-item-text-builder">
                    由 @{props.roleplay.builder} 创建
                </div>
                <div className="roleplay-card-item-text-desc">
                    {props.roleplay.desc}
                </div>
                <div className="roleplay-card-item-text-like-container">
                    <div>
                        <img src={require("../imgs/like.png")} className="roleplay-card-item-text-like-icon"/>
                    </div>
                    <div className="roleplay-card-item-text-like-count">
                        {props.roleplay.likeCount}
                    </div>
                </div>
            </div>
        </div>
    )
};

export default RoleplayCardItem;