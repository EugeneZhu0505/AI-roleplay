import { useState, useEffect } from 'react';
import AudioItem from './audioItem';
import './styles/audioSlide.css';

const AudioSlide = ({audioList}) =>{
    const [itemsPerPage, setItemsPerPage] = useState(4);
    const [visibleItems, setVisibleItems] = useState([]);
    const [currentIndex, setCurrentIndex] = useState(0);

    useEffect(() => {
        setItemsPerPage(audioList.length > 4 ? 4 : audioList.length);
    }, [])

    useEffect(() => {
        const newvisibleItems = audioList.slice(currentIndex, currentIndex + itemsPerPage);
        setVisibleItems(newvisibleItems);

    }, [currentIndex]);

    const nextSlide = () => {
        if (currentIndex + 1 <= audioList.length-itemsPerPage) {
            setCurrentIndex(currentIndex + 1);
        }
    }
    const prevSlide = () => {
        if (currentIndex - 1 >= 0) {
            setCurrentIndex(currentIndex - 1);
        }
    }

    return(
        <div className="audioList-voice-list-container">
            <div className="prevButton-container">
                <img className={`prevButton ${currentIndex === 0 ? 'disabled' : ''}`} src={require('../imgs/prev.png')} onClick={prevSlide}/>
            </div>

            <div className='audioItem-voice-container'>
                {visibleItems.map((audioItem, index) => (
                    <AudioItem audio={{
                        key: index,
                        cover: audioItem[1],
                        name: audioItem[0],
                        audio: audioItem[2],
                    }} />
                ))}
            </div>
            
            <div className="nextButton-container">
                <img className={`nextButton ${currentIndex === (audioList.length - itemsPerPage) ? "disabled": ""}`} src={require('../imgs/next.png')} onClick={nextSlide}/>
            </div>
        </div>
    )
}

export default AudioSlide
