import "./styles/roleplayCardItem.css"


const RoleplayCardItem = (props) => {


    const handleClick = () => {
        props.roleplay.handleRoleplayCardClick({
            key: props.roleplay.id,
            cover: props.roleplay.cover,
            name: props.roleplay.name,
            builder: props.roleplay.builder,
            desc: props.roleplay.desc,
            likeCount: props.roleplay.likeCount,
        })
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
}

export default RoleplayCardItem;