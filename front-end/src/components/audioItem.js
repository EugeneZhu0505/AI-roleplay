import React from 'react';
import './styles/audioItem.css';

let currentAudioElement = null;

const AudioItem = React.memo(({audio}) => {

    const handleSelectAudio = () => {
        if (currentAudioElement) {
            currentAudioElement.pause();

            if(currentAudioElement.src === audio.audio) {
                currentAudioElement = null;
                return;
            }
        }
        const newAudioElement = new Audio(audio.audio);
        newAudioElement.play();
        currentAudioElement = newAudioElement;
        newAudioElement.addEventListener('ended', () => {
            currentAudioElement = null;
        });  
    };



    return (
        <div className='audioItem-container'>
            <div className="audioItem-content">
                <div className="audioItem-broad-container">
                    <img className='audioItem-cover' src={audio.cover} />
                    <img className='audioItem-broad-icon' src={require("../imgs/broad.png")} onClick={handleSelectAudio}/>
                </div>
                <div className='audioItem-info'>
                    <p className='audioItem-name'>{audio.name}</p>
                </div>
            </div>
        </div>
    );
});

export default AudioItem;