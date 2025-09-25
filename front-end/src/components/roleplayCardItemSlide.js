import { useState, useEffect } from 'react';
import RoleplayCardItem from './roleplayCardItem';
import './styles/roleplayCardItemSlide.css';

const images = [
    '0',
    '1',
    '2',
    '3', 
    '4', 
    '5', 
    '6', 
    '7', 
];

const RoleplayListSlide = () => {
    const itemsPerPage = 4;
    const [visibleItems, setVisibleItems] = useState([]);
    const [currentIndex, setCurrentIndex] = useState(0);

    useEffect(() => {
        setCurrentIndex(4);
    }, [])

    useEffect(() => {
        const newvisibleItems = images.slice(currentIndex, currentIndex + itemsPerPage);
        setVisibleItems(newvisibleItems);

    }, [currentIndex]);


    const nextSlide = () => {
        if (currentIndex + 1 <= images.length-itemsPerPage) {
            setCurrentIndex(currentIndex + 1);
        }
    }
    const prevSlide = () => {
        if (currentIndex - 1 >= 0) {
            setCurrentIndex(currentIndex - 1);
        }
    }

    return (
        <div className='roleplayCardItem-slide-container'>
            <div className="prevButton-container">
                <img className={`prevButton ${currentIndex === 0 ? 'disabled' : ''}`} src={require('../imgs/prev.png')} onClick={prevSlide}/>
            </div>
            <div className="roleplayCardItem-slide">
                {visibleItems.map((image, index) => (
                    <RoleplayCardItem roleplay={{
                            cover: 'https://characterai.io/i/200/static/avatars/uploaded/2023/11/29/LC67szCU6GtiPnevTnUdJ1N8UDhXQbEVxnG0R7tw4js.webp?webp=true&anim=0',
                            name: `角色${image}`,
                            builder: `构建者${image}`,
                            desc: '这是一个角色的描述',
                            likeCount: "10M",
                    }} />
                ))}
            </div>
            <div className="nextButton-container">
                <img className={`nextButton ${currentIndex === (images.length - itemsPerPage) ? "disabled": ""}`} src={require('../imgs/next.png')} onClick={nextSlide}/>
            </div>
        </div>
    );
};

export default RoleplayListSlide;
