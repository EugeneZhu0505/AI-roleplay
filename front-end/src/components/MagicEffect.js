import { useRef } from 'react';
import { useFrame } from '@react-three/fiber';
import { PointsMaterial } from 'three';

// 魔法效果组件
function MagicEffect() {
  const particlesRef = useRef();

   const materialRef = useRef(new PointsMaterial({
    color: "#9c27b0",
    size: 0.05,
    transparent: true,
    opacity: 0.8,
    sizeAttenuation: true
  }));
  
  useFrame(({ clock }) => {
    const time = clock.getElapsedTime();
    if (particlesRef.current) {
      particlesRef.current.rotation.y = time * 0.2;
      particlesRef.current.rotation.x = Math.sin(time * 0.5) * 0.1;
    }
  });
  
  return (
    <group ref={particlesRef} position={[0, 2.8, 0]}>
      <points material={materialRef.current}>
        <sphereGeometry args={[0.5, 16, 16]} />
      </points>
    </group>
  );
}