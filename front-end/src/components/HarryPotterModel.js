import { useRef } from 'react';
import { useFrame } from '@react-three/fiber';

function HarryPotterModel() {
  const groupRef = useRef();
  const headRef = useRef();
  const leftArmRef = useRef();
  const rightArmRef = useRef();
  
  // 动画效果
  useFrame(({ clock }) => {
    const time = clock.getElapsedTime();
    
    // 身体轻微旋转
    if (groupRef.current) {
      groupRef.current.rotation.y = Math.sin(time * 0.5) * 0.1;
    }
    
    // 头部轻微摆动
    if (headRef.current) {
      headRef.current.rotation.x = Math.sin(time * 0.7) * 0.05;
    }
    
    // 手臂摆动
    if (leftArmRef.current) {
      leftArmRef.current.rotation.z = Math.sin(time * 1.2) * 0.5;
    }
    
    if (rightArmRef.current) {
      rightArmRef.current.rotation.z = -Math.sin(time * 1.2) * 0.5;
    }
  });

  return (
    <group ref={groupRef} position={[0, -1, 0]}>
      {/* 身体 */}
      <mesh position={[0, 0.8, 0]}>
        <cylinderGeometry args={[0.4, 0.5, 1.5, 8]} />
        <meshStandardMaterial color="#2c3e50" />
      </mesh>
      
      {/* 头部 */}
      <mesh ref={headRef} position={[0, 2.1, 0]}>
        <sphereGeometry args={[0.6, 16, 16]} />
        <meshStandardMaterial color="#f0d9b5" />
      </mesh>
      
      {/* 头发 */}
      {/* <mesh position={[0, 2.8, 0]}>
        <sphereGeometry args={[0.7, 16, 16]} />
        <meshStandardMaterial color="#3c2e2e" />
      </mesh> */}
      
      {/* 刘海 */}
      {/* <mesh position={[0, 2.4, 0.4]}>
        <cylinderGeometry args={[0.5, 0.3, 0.3, 8]} rotation={[0, 0, Math.PI/2]} />
        <meshStandardMaterial color="#3c2e2e" />
      </mesh> */}
      
      {/* 眼镜 */}
      <mesh position={[0.2, 2.2, 0.6]}>
        <torusGeometry args={[0.1, 0.03, 12, 12]} />
        <meshStandardMaterial color="#000000" />        
      </mesh>

      <mesh position={[-0.2, 2.2, 0.6]}>
        <torusGeometry args={[0.1, 0.03, 16, 16]} />
        <meshStandardMaterial color="#000000" />       
      </mesh>
      
      <mesh position={[-0.2, 2.2, 0.56]}>
        <sphereGeometry args={[0.06]} />
        <meshStandardMaterial color="#000000" transparent opacity={0.7} />
      </mesh>
      
      <mesh position={[0.2, 2.2, 0.56]}>
        <sphereGeometry args={[0.06]} />
        <meshStandardMaterial color="#000000" transparent opacity={0.7} />
      </mesh>
      
      {/* 围巾 - 哈利波特标志性的格兰芬多围巾 */}
      {/* <mesh position={[0, 1.6, 0]} rotation={[0, Math.PI/2, 0]}>
        <cylinderGeometry args={[0.6, 0.6, 0.6, 8]} />
        <meshStandardMaterial color="#7F0909" />
      </mesh>
      <mesh position={[0, 1.3, 0]} rotation={[0, Math.PI/2, 0]}>
        <cylinderGeometry args={[0.6, 0.6, 0.3, 8]} />
        <meshStandardMaterial color="#FFC500" />
      </mesh> */}
      
      {/* 手臂 */}
      <group ref={leftArmRef} position={[-0.5, 1.2, 0]}>
        <mesh>
          <cylinderGeometry args={[0.15, 0.15, 0.8, 8]} rotation={[0, 0, Math.PI/2]} />
          <meshStandardMaterial color="#2c3e50" />
        </mesh>
        <mesh position={[0.0, -0.6, 0]}>
          <sphereGeometry args={[0.15]} />
          <meshStandardMaterial color="#f0d9b5" />
        </mesh>
      </group>
      
      <group ref={rightArmRef} position={[0.5, 1.2, 0]}>
        <mesh>
          <cylinderGeometry args={[0.15, 0.15, 0.8, 8]} rotation={[0, 0, Math.PI/2]} />
          <meshStandardMaterial color="#2c3e50" />
        </mesh>
        <mesh position={[0.0, -0.6, 0]}>
          <sphereGeometry args={[0.15]} />
          <meshStandardMaterial color="#f0d9b5" />
        </mesh>
      </group>
      
      {/* 腿 */}
      <mesh position={[-0.2, 0, 0]}>
        <cylinderGeometry args={[0.2, 0.2, 1.0, 8]} rotation={[0, 0, 0]} />
        <meshStandardMaterial color="#1a252f" />
      </mesh>
      <mesh position={[0.2, 0, 0]}>
        <cylinderGeometry args={[0.2, 0.2, 1.0, 8]} rotation={[0, 0, 0]} />
        <meshStandardMaterial color="#1a252f" />
      </mesh>
      
      {/* 鞋子 */}
      <mesh position={[-0.2, -0.5, 0]}>
        <cylinderGeometry args={[0.25, 0.25, 0.2, 8]} />
        <meshStandardMaterial color="#000000" />
      </mesh>
      <mesh position={[0.2, -0.5, 0]}>
        <cylinderGeometry args={[0.25, 0.25, 0.2, 8]} />
        <meshStandardMaterial color="#000000" />
      </mesh>
    </group>
  );
}