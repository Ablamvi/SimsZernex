# MaisonVie 3D — Phase 1 verification

- House scene binary uses HSCN v1 with 240 vertices / 1080 indices.
- House bounds are approximately X/Z ±4.65 m and Y -0.12..2.75 m.
- Character skinned asset: 84,603 vertices, 305,808 indices, 102 bones.
- Rest-pose skinned height is approximately 3.279 units; runtime scale corrected to 0.01 => ~1.80 m.
- Camera is now an orbit camera with yaw/pitch/radius state.
- One-finger tap selects floor/furniture; one-finger drag orbits camera.
- Two-finger pinch zooms.
- Character starts near the center of the room to guarantee initial visibility.
- Sofa gameplay coordinates corrected to its actual GLB footprint near z=0.45.
