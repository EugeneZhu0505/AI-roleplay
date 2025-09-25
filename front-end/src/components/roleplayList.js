import "./styles/roleplayList.css"
import RoleplayCardItem from './roleplayCardItem';
import RoleplayListSlide from './roleplayCardItemSlide';

const RoleplayList = (props) => {


    return(
        <div className="roleplayList-container">

            {/** 页面头*/}
            <div className="roleplayList-header">
                <div className="roleplayList-header-welcome-comtainer">
                    <div className="roleplayList-header-welcome">
                        欢迎回来,
                    </div>
                    <div className="roleplayList-header-user">
                        <img className="roleplayList-header-user-img" src={props.image==="" ? props.image : require("../imgs/place_charac_image.png")}/>
                        <div className="roleplayList-header-user-name">
                            {props.username}
                        </div>
                    </div>
                </div>
                <div className="roleplayList-header-search-container">
                    <img className="roleplayList-header-search-icon" src={require("../imgs/search-icon.png")}/>
                    <input className="roleplayList-header-search" type="text" placeholder="搜索" />
                </div>
            </div>

            {/** 热门推荐角色列表 */}
            <div className="roleplayList-recommend-container">
                <div className="roleplayList-recommend-header">
                    热门推荐
                </div>

                <div className="roleplayList-recommend-list-container">
                    <RoleplayListSlide  handleRoleplayCardClick={props.handleRoleplayCardClick}/>
                </div>
            </div>


            {/** 分类列表 */}
            <div className="roleplayList-category-container">
                <div className="roleplayList-category-button-container">
                    <button className="roleplayList-category-button">动漫</button>
                    <button className="roleplayList-category-button">影视</button>
                    <button className="roleplayList-category-button">历史</button>
                    <button className="roleplayList-category-button">科普</button>
                </div>

                <div className="roleplayList-category-list-container">
                    <RoleplayListSlide  handleRoleplayCardClick={props.handleRoleplayCardClick}/>
                </div>
            </div>


            {/** 语音分类 */}
            <div className="rolepalyList-voice-container">
                <div className="rolepalyList-voice-header">
                    语音
                </div>

                
            </div>

        </div>
    )
}

export default RoleplayList;