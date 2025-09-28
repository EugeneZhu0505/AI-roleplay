import './styles/roleplaySearchItem.css';
import { useContext } from 'react';
import {HistoryAICharacter, SelectedAICharacter} from './utils/historyAICharacter';

const RoleplaySearchItem = ({searchItem}) => {
    const {setSelectedRoleplay, setIsChat} = useContext(SelectedAICharacter);

    const handleClickSearchItem = () => {
        setSelectedRoleplay({
            characterId: searchItem.id,
            name: searchItem.name,
            cover: searchItem.avatarUrl,
            description: searchItem.description,
            personality: searchItem.personality,
        })
        setIsChat(true);
    }

    return(
        <div className="roleplaySearchItem-container" onClick={handleClickSearchItem}>
            <img className="roleplaySearchItem-character-avatar" src={searchItem.avatarUrl} />
            <div className="roleplaySearchItem-character-text">
                <div className="roleplaySearchItem-character-name">{searchItem.name}</div>
                <div className="roleplaySearchItem-character-desription">{searchItem.description}</div>
                <div className="roleplaySearchItem-character-personality">{searchItem.personality}</div>
            </div>
        </div>
    )
}

export default RoleplaySearchItem;
