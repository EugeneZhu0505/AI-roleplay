import "./styles/roleplayCardItem.css"


const RoleplayCardItem = (props) => {

    return(
        <div className="roleplay-card-item-container">
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
}

export default RoleplayCardItem;