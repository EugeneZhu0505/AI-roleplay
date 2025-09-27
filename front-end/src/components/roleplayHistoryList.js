import RoleplayHistoryListItem from './roleplayHistoryListItem'
import './styles/roleplayHistoryList.css'

const RoleplayHistoryAI = [
    {   
        id: 1,
        name: '角色1',
        avatar: 'https://characterai.io/i/80/static/avatars/uploaded/2023/2/3/CcwSs9WLY9fmHnGsRkNDR-5Wt_5hD-U1K2dFcXvv8lM.webp?webp=true&anim=0',
    },
    {
        id: 2,
        name: '角色2',
        avatar: 'https://characterai.io/i/80/static/avatars/uploaded/2023/3/2/yGY_jD45XZCo55HvB2apKrqtfVOGhU1PiCkWmXA2jCA.webp?webp=true&anim=0',
    },
    {
        id: 3,
        name: '角色1',
        avatar: 'https://characterai.io/i/80/static/avatars/uploaded/2023/2/3/CcwSs9WLY9fmHnGsRkNDR-5Wt_5hD-U1K2dFcXvv8lM.webp?webp=true&anim=0',
    },
    {
        id: 4,
        name: '角色2',
        avatar: 'https://characterai.io/i/80/static/avatars/uploaded/2023/3/2/yGY_jD45XZCo55HvB2apKrqtfVOGhU1PiCkWmXA2jCA.webp?webp=true&anim=0',
    },
    {
        id: 5,
        name: '角色1',
        avatar: 'https://characterai.io/i/80/static/avatars/uploaded/2023/2/3/CcwSs9WLY9fmHnGsRkNDR-5Wt_5hD-U1K2dFcXvv8lM.webp?webp=true&anim=0',
    },
    {
        id: 6,
        name: '角色2',
        avatar: 'https://characterai.io/i/80/static/avatars/uploaded/2023/3/2/yGY_jD45XZCo55HvB2apKrqtfVOGhU1PiCkWmXA2jCA.webp?webp=true&anim=0',
    },
    {
        id: 7,
        name: '角色1',
        avatar: 'https://characterai.io/i/80/static/avatars/uploaded/2023/2/3/CcwSs9WLY9fmHnGsRkNDR-5Wt_5hD-U1K2dFcXvv8lM.webp?webp=true&anim=0',
    },
    {
        id: 8,
        name: '角色2',
        avatar: 'https://characterai.io/i/80/static/avatars/uploaded/2023/3/2/yGY_jD45XZCo55HvB2apKrqtfVOGhU1PiCkWmXA2jCA.webp?webp=true&anim=0',
    },
    {
        id: 9,
        name: '角色1',
        avatar: 'https://characterai.io/i/80/static/avatars/uploaded/2023/2/3/CcwSs9WLY9fmHnGsRkNDR-5Wt_5hD-U1K2dFcXvv8lM.webp?webp=true&anim=0',
    },
    {
        id: 10,
        name: '角色2',
        avatar: 'https://characterai.io/i/80/static/avatars/uploaded/2023/3/2/yGY_jD45XZCo55HvB2apKrqtfVOGhU1PiCkWmXA2jCA.webp?webp=true&anim=0',
    },

]


const RoleplayHistoryList = () => {
    return(     
        <div className="roleplay-history-list-container">
            <div className="roleplay-history-list-title">
                历史对话角色
            </div>
            <div className="roleplay-history-list-item-container">
                {
                    RoleplayHistoryAI.map((item, index) => {
                        return <RoleplayHistoryListItem item={item} key={index} />
                    })
                }
            </div>

        </div>
    )
}

export default RoleplayHistoryList