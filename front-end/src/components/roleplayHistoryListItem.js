import './styles/roleplayHistoryListItem.css'

const RoleplayHistoryListItem = (props) => {


    return(
        <div className="roleplay-history-card-container" key={props.item.id}>
            <img src={props.item.avatar} className="roleplay-history-list-item-avatar" />
            <div className="roleplay-history-list-item-name">
                {props.item.name}
            </div>
        </div>
    )
}

export default RoleplayHistoryListItem
