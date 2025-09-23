import React, { useRef, useState, useEffect } from 'react';
import { Canvas, useFrame, useThree } from '@react-three/fiber';
import { OrbitControls, Environment, useGLTF, Text, useTexture, MeshReflectorMaterial } from '@react-three/drei';
import { Button, TextField, Box, Grid, Paper, Typography, CircularProgress, Alert } from '@mui/material';
import * as THREE from 'three';

// 预加载纹理资源
const useWolfTextures = () => {
  // 加载毛皮纹理
  const furTexture = useTexture('./model/wolf/gltf/fur__fella3_jpg_001_diffuse.png');
  const furAlpha = useTexture('./model/wolf/gltf/Fur_Alpha_3.png');
  const furCol = useTexture('./model/wolf/gltf/Fur_Col_20.png');
  
  // 加载眼睛纹理
  const eyesTexture = useTexture('./model/wolf/gltf/eyes_diffuse.jpeg');
  
  // 设置纹理属性
  useEffect(() => {
    if (furTexture) {
      furTexture.wrapS = furTexture.wrapT = THREE.RepeatWrapping;
      furTexture.repeat.set(2, 2);
    }
    if (furCol) {
      furCol.wrapS = furCol.wrapT = THREE.RepeatWrapping;
      furCol.repeat.set(2, 2);
    }
    if (eyesTexture) {
      eyesTexture.magFilter = THREE.LinearFilter;
    }
  }, [furTexture, furCol, eyesTexture]);
  
  return {
    furTexture,
    furAlpha,
    furCol,
    eyesTexture
  };
};

// GLTF模型加载组件 - 支持点击触发不同动作
const GLTFModel = ({ path, scale = 1, position = [0, 0, 0], onLoaded, onError }) => {
  const { gl } = useThree(); // 获取WebGL渲染上下文
  const group = useRef();   // 模型容器
  const mixer = useRef();   // 动画混合器
  const actions = useRef({}); // 存储所有动画动作
  const [currentAction, setCurrentAction] = useState('idle');  // 当前播放的动画动作
  const [modelStructure, setModelStructure] = useState(''); // 存储模型结构信息
  
  // 使用useGLTF加载GLTF/GLB模型
  const { scene, animations, materials, nodes, error, loading } = useGLTF(path);
  const model = scene; // GLTF模型的主场景对象
  
  // 加载纹理资源
  const textures = useWolfTextures();
  
  // 增强模型材质
  useEffect(() => {
    try {
      if (model && materials && Object.values(materials).length > 0) {
        console.log('增强模型材质...');
        // 遍历所有材质，尝试应用额外的纹理
        Object.values(materials).forEach((material, index) => {
          try {
            if (!material) return;
            
            // 为毛皮材质添加细节纹理
            if (material.name && (material.name.toLowerCase().includes('fur') || material.name.toLowerCase().includes('skin'))) {
              if (textures.furTexture) {
                material.map = textures.furTexture;
                console.log(`为材质 ${material.name} 添加毛皮纹理`);
              }
              if (textures.furAlpha) {
                material.alphaMap = textures.furAlpha;
                material.transparent = true;
                console.log(`为材质 ${material.name} 添加alpha纹理`);
              }
            }
            // 为眼睛材质添加细节
            if (material.name && material.name.toLowerCase().includes('eye')) {
              if (textures.eyesTexture) {
                material.map = textures.eyesTexture;
                console.log(`为材质 ${material.name} 添加眼睛纹理`);
              }
            }
            // 更新材质
            material.needsUpdate = true;
          } catch (e) {
            console.error(`更新材质索引 ${index} 时出错:`, e);
          }
        });
      }
    } catch (error) {
      console.error('增强模型材质过程中出错:', error);
    }
  }, [model, materials, textures]);
  
  // 模型加载完成回调
  useEffect(() => {
    if (model && !loading && !error) {
      onLoaded?.();
    }
  }, [model, loading, error, onLoaded]);

  // 处理模型初始化（位置、缩放、动画）
  useEffect(() => {
    console.log('开始初始化GLTF模型...');
    if (!model) {
      console.log('模型对象不存在');
      return;
    }
    
    try {
      // 打印模型结构，帮助调试
      console.log('模型结构:');
      let structureInfo = [];
      if (model && typeof model.traverse === 'function') {
        model.traverse((child) => {
          // 简化结构信息，只记录关键属性
          const info = {
            name: child.name,
            type: child.type,
            position: child.position ? `${child.position.x.toFixed(2)},${child.position.y.toFixed(2)},${child.position.z.toFixed(2)}` : 'N/A',
            visible: child.visible,
            geometry: child.geometry ? child.geometry.type : 'N/A'
          };
          structureInfo.push(`${info.name || 'Unnamed'} (${info.type}) - pos:${info.position} - geom:${info.geometry}`);
        });
        console.log(structureInfo.join('\n'));
        // 注释掉UI显示，避免不必要的复杂性
        // setModelStructure(structureInfo.join('\n'));
      }
      
      // 移除圆盘底座 - 增强版
      console.log('移除圆盘底座...');
      
      // 方法1: 直接遍历模型场景中的所有网格，移除看起来像底座的对象
      let discRemoved = false;
      if (model && typeof model.traverse === 'function') {
        model.traverse((child) => {
          // 检查是否是网格对象
          if (child.isMesh) {
            // 检查几何体类型和属性来识别圆盘
            const geometry = child.geometry;
            if (geometry) {
              // 检查是否是圆盘形状
              const isDiscShape = 
                // 1. CircleGeometry 直接就是圆盘
                geometry.type === 'CircleGeometry' ||
                // 2. PlaneGeometry 可能是扁平底座
                (geometry.type === 'PlaneGeometry' && 
                 geometry.parameters && 
                 geometry.parameters.width > 5 && 
                 geometry.parameters.height > 5) ||
                // 3. 检查是否是圆形平面（通过顶点判断）
                (geometry.attributes.position && 
                 geometry.attributes.position.count > 100 &&
                 // 假设圆盘在模型底部
                 child.position.y < 0);
              
              if (isDiscShape) {
                // 记录并移除
                console.log('找到并移除圆盘对象:', child.name || child.type);
                if (child.parent) {
                  child.parent.remove(child);
                  discRemoved = true;
                }
              }
            }
          }
        });
      }
      
      if (!discRemoved) {
        console.log('未找到明显的圆盘对象，尝试备选方案...');
        // 方法2: 创建一个新的场景，只包含狼的主体部分
        const newScene = new THREE.Group();
        if (model && typeof model.traverse === 'function') {
          model.traverse((child) => {
            // 排除底部扁平对象
            if (child.isMesh && 
                child.geometry && 
                child.geometry.type !== 'CircleGeometry' &&
                !(child.geometry.type === 'PlaneGeometry' && child.position.y < 0)) {
              // 克隆并添加到新场景
              const clonedChild = child.clone();
              newScene.add(clonedChild);
            }
          });
          
          // 如果新场景有内容，替换原模型
          if (newScene.children.length > 0) {
            console.log('使用筛选后的模型，已排除可能的圆盘');
            model.clear(); // 清空原模型
            // 将新场景的子对象添加回原模型
            while(newScene.children.length > 0) {
              model.add(newScene.children[0]);
            }
          }
        }
      }
      
      // 自动调整模型位置和缩放
      if (model) {
        const box = new THREE.Box3().setFromObject(model);
        const size = box.getSize(new THREE.Vector3());
        const center = box.getCenter(new THREE.Vector3());
        
        // 居中模型
        model.position.x = model.position.x - center.x;
        model.position.y = model.position.y - center.y;
        
        // 自动缩放以适应视图
        const maxDim = Math.max(size.x, size.y, size.z);
        const scaleVal = 5 / maxDim; // 调整这个值控制模型大小
        model.scale.set(scaleVal, scaleVal, scaleVal);
        
        // 向上移动模型足够的距离，使圆盘完全隐藏在视图外
        model.position.y = 1.5;
      }
    } catch (error) {
      console.error('模型初始化过程中出错:', error);
    }
    
    // 记录模型信息用于调试
    console.log('GLTF模型加载完成，信息:', {
      childrenCount: model.children.length,
      hasAnimations: animations && animations.length > 0,
      animationsCount: animations ? animations.length : 0,
      modelType: model.type,
      materialsCount: materials ? Object.keys(materials).length : 0,
      nodesCount: nodes ? Object.keys(nodes).length : 0
    });
    
    if (animations.length > 0) {
      mixer.current = new THREE.AnimationMixer(model);
      
      // 存储所有动画动作
      animations.forEach((clip, index) => {
        try {
          const action = mixer.current.clipAction(clip);
          const actionName = clip.name.toLowerCase();
          actions.current[actionName] = action;
          console.log(`动画${index + 1}: ${clip.name} (${actionName})`);
        } catch (e) {
          console.error(`创建动画动作时出错 (${clip.name}):`, e);
        }
      });
      
      const loadedActionNames = Object.keys(actions.current);
      console.log('成功加载的动画:', loadedActionNames);
      
      // 自动播放idle动画（如果存在）
      if (actions.current.idle) {
        playAction('idle');
        console.log('自动播放idle动画');
      } else if (loadedActionNames.length > 0) {
        // 如果没有idle动画，播放第一个动画
        playAction(loadedActionNames[0]);
        console.log(`自动播放第一个动画: ${loadedActionNames[0]}`);
      } else {
        console.warn('没有成功创建任何动画动作');
        // 添加默认的模拟动画，以防动画加载失败
        actions.current = {
          idle: { play: () => {}, pause: () => {}, reset: () => {}, stop: () => {} },
          walk: { play: () => {}, pause: () => {}, reset: () => {}, stop: () => {} },
          run: { play: () => {}, pause: () => {}, reset: () => {}, stop: () => {} }
        };
      }
    }
  }, [model]);

  // 更新动画混合器
  useFrame((_, delta) => {
    if (mixer.current) {
      mixer.current.update(delta);
    }
  });

  // 播放动画的函数
  const playAction = (actionName) => {
    // 暂停所有其他动画
    Object.keys(actions.current).forEach(name => {
      if (name !== actionName && actions.current[name]) {
        actions.current[name].stop();
      }
    });
    
    // 播放选中的动画
    const action = actions.current[actionName];
    if (action) {
      action.reset();
      action.play();
      setCurrentAction(actionName);
      
      // 5秒后恢复到idle动画（如果存在）
      setTimeout(() => {
        if (actions.current.idle) {
          playAction('idle');
        }
      }, 5000);
    }
  };

  // 处理点击事件
  const handleClick = (event) => {
    event.stopPropagation(); // 防止事件冒泡
    
    // 获取所有可用动作
    const availableActions = Object.keys(actions.current);
    
    if (availableActions.length === 0) {
      console.log('没有可用的动画');
      return;
    }
    
    // 随机选择一个不同的动作
    let nextAction;
    do {
      nextAction = availableActions[Math.floor(Math.random() * availableActions.length)];
    } while (availableActions.length > 1 && nextAction === currentAction);
    
    console.log(`切换到动画: ${nextAction}`);
    playAction(nextAction);
  };

  // 错误处理
  if (error) {
    onError?.(error);
    return (
      <Text 
        position={[0, 0, 0]} 
        fontSize={0.5} 
        color="red"
        anchorX="center"
        anchorY="center"
      >
        加载失败: {error.message}
      </Text>
    );
  }

  // 加载中
  if (loading || !model) {
    return (
      <Text 
        position={[0, 0, 0]} 
        fontSize={0.5} 
        color="blue"
        anchorX="center"
        anchorY="center"
      >
        加载中...
      </Text>
    );
  }
  
  // 确保模型存在再访问其属性
  if (!model) {
    return (
      <Text 
        position={[0, 0, 0]} 
        fontSize={0.5} 
        color="red"
        anchorX="center"
        anchorY="center"
      >
        模型加载失败
      </Text>
    );
  }

  // 渲染可点击的模型
  return (
    <group 
      ref={group} 
      position={position} 
      scale={scale}
      onClick={handleClick}
      onPointerOver={() => {
        if (gl && gl.canvas) {
          gl.canvas.style.cursor = 'pointer';
        }
      }}
      onPointerOut={() => {
        if (gl && gl.canvas) {
          gl.canvas.style.cursor = 'default';
        }
      }}
    >
      {model && <primitive object={model} dispose={null} />}
      
      {/* 添加一个不可见的点击区域以增强点击体验 */}
      <mesh 
        position={[0, 1, 0]} 
        onClick={handleClick}
        visible={false}
      >
        <sphereGeometry args={[3, 16, 16]} />
        <meshBasicMaterial transparent={true} opacity={0} />
      </mesh>
    </group>
  );
};


// 装饰性元素组件 - 必须放在Canvas内部使用R3F hooks
const DecorativeElements = () => {
  // 在Canvas内部使用纹理资源
  const textures = useWolfTextures();
  
  return (
    <>
      {/* 添加装饰性草丛 - 使用纹理资源 */}
      <group position={[-4, 0, 0]}>
        <mesh scale={[1, 1.5, 1]}>
          <cylinderGeometry args={[0.3, 0.3, 1]} />
          <meshStandardMaterial 
            map={textures.furTexture} 
            color={0x228B22} 
            roughness={0.8}
          />
        </mesh>
      </group>
      
      <group position={[4, 0, 0]}>
        <mesh scale={[1, 1.2, 1]}>
          <cylinderGeometry args={[0.3, 0.3, 1]} />
          <meshStandardMaterial 
            map={textures.furTexture} 
            color={0x228B22} 
            roughness={0.8}
          />
        </mesh>
      </group>
    </>
  );
};

// 主组件
const TomCatModel = () => {
  const [isModelLoaded, setIsModelLoaded] = useState(false);
  const [loadError, setLoadError] = useState(null);
  // 选择带有动画的GLB文件 - GLB是包含所有资源的二进制格式
  const modelPath = './model/wolf/gltf/Wolf-Blender-2.82a.glb'; // 使用优化的二进制GLTF格式


  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '600px', width: '600px' }}>
      {/* 3D渲染区域 */}
      <Box sx={{ flexGrow: 1, position: 'relative' }}>
        <Canvas 
          shadows
          camera={{ position: [5, 3, 8], fov: 50 }}
          style={{ background: 'linear-gradient(to bottom, #e0f7fa,rgb(161, 168, 169))' }}
        >
          {/* 光源设置 */}
        <ambientLight intensity={0.5} />
        <directionalLight 
          position={[10, 10, 5]} 
          intensity={1} 
          castShadow 
          shadow-mapSize={[2048, 2048]}
        />
        <pointLight position={[-5, 5, 5]} intensity={0.3} />
        
        {/* 只保留狼的模型 */}
          
          {/* 加载模型 - 使用GLTF加载方式 */}
          <GLTFModel 
            path={modelPath}
            position={[0, 1, 0]} // 向上移动模型1个单位，避开圆盘
            onLoaded={() => setIsModelLoaded(true)}
            onError={(err) => setLoadError(`模型加载失败: ${err.message}`)}
          />
          
          <Environment preset="forest" />
          <OrbitControls 
            enableZoom={true} 
            enablePan={true}
            maxPolarAngle={Math.PI / 2}
          />
        </Canvas>

        {/* 加载状态提示 */}
        {!isModelLoaded && !loadError && (
          <Box 
            sx={{ 
              position: 'absolute', 
              top: '50%', 
              left: '50%', 
              transform: 'translate(-50%, -50%)' 
            }}
          >

          </Box>
        )}

        {/* 错误提示 */}
        {loadError && (
          <Box 
            sx={{ 
              position: 'absolute', 
              top: 20, 
              left: '50%', 
              transform: 'translateX(-50%)', 
              width: '80%',
              maxWidth: 500
            }}
          >
            <Alert severity="error" sx={{ backgroundColor: 'rgba(255, 255, 255, 0.9)' }}>
              <Typography variant="body1">{loadError}</Typography>
              <Typography variant="body2" sx={{ mt: 1 }}>
                排查步骤:
                1. 确认文件存在于 {modelPath}
                2. 文件名大小写是否正确
                3. 尝试将模型移动到public根目录，路径改为 './Fox.glb'
              </Typography>
            </Alert>
          </Box>
        )}
      </Box>
      
    </Box>
  );
};

export default TomCatModel;
