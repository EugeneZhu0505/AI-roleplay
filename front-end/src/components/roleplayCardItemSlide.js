import { useState, useEffect } from 'react';
import RoleplayCardItem from './roleplayCardItem';
import './styles/roleplayCardItemSlide.css';
import React from "react";


const RoleplayListSlide = React.memo((props) => {
    const [itemsPerPage, setItemsPerPage] = useState(4);
    const [visibleItems, setVisibleItems] = useState([]);
    const [currentIndex, setCurrentIndex] = useState(0);


    useEffect(() => {
        setItemsPerPage(props.roleplayList.length > 4 ? 4 : props.roleplayList.length);
    }, [])

    useEffect(() => {
        const newvisibleItems = props.roleplayList.slice(currentIndex, currentIndex + itemsPerPage);
        setVisibleItems(newvisibleItems);

    }, [currentIndex]);


    const nextSlide = () => {
        if (currentIndex + 1 <= props.roleplayList.length-itemsPerPage) {
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
                {visibleItems.map((roleplayItem, index) => (
                    <RoleplayCardItem roleplay={{
                            key: index,
                            id: roleplayItem.id,
                            cover: roleplayItem.avatarUrl,
                            name: roleplayItem.name,
                            builder: "admin",
                            desc: roleplayItem.description,
                            likeCount: "10M",
                    }} />
                ))}
            </div>
            <div className="nextButton-container">
                <img className={`nextButton ${currentIndex === (props.roleplayList.length - itemsPerPage) ? "disabled": ""}`} src={require('../imgs/next.png')} onClick={nextSlide}/>
            </div>
        </div>
    );
});

export default RoleplayListSlide;
