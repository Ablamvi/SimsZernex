from pathlib import Path
import struct, zipfile, re, math
root=Path(__file__).resolve().parents[1]
char=root/'app/src/main/assets/models/character.skinnedbin'
scene=root/'app/src/main/assets/models/house.scene.bin'
cpp=root/'app/src/main/cpp'
assert char.read_bytes()[:4]==b'ZSKN'
b=char.read_bytes(); vc,ic,bc,res=struct.unpack_from('<4I',b,4); assert (vc,ic,bc)==(84603,305808,102)
o=20+vc*60+ic*4
assert o < len(b)
for _ in range(bc):
    parent,_,nl=struct.unpack_from('<iiI',b,o); o+=12+64+64
    assert nl<=256 and o+nl<=len(b); o+=nl
# Validate all vertex joints/weights without numpy.
for i in range(vc):
    off=20+i*60
    js=struct.unpack_from('<4H',b,off+24); ws=struct.unpack_from('<4f',b,off+32)
    assert all(j<bc for j in js)
    assert abs(sum(ws)-1.0)<2e-4
sb=scene.read_bytes(); assert sb[:4]==b'HSCN'; version,svc,sic=struct.unpack_from('<III',sb,4); assert version==1
assert 16+svc*36+sic*4==len(sb)
assert svc==528 and sic==1368
# 240 original vertices + 8 interior wall cubes, each wall cube is 36 unique verts/36 indices.
assert svc==240+8*36 and sic==30*12*3+8*12*3
assert max(struct.unpack_from('<%dI'%(sic),sb,16+svc*36)) < svc
# Check the exact internal wall bounds expected by the layout.
import array
verts=array.array('f'); verts.frombytes(sb[16:16+svc*36])
for i in range(240,svc):
    x,y,z=verts[i*9:i*9+3]
    assert -4.65<=x<=4.65 and -0.12<=y<=2.75 and -4.65<=z<=4.65
# C++/Kotlin JNI names and version consistency.
native=(cpp/'native-lib.cpp').read_text(); bridge=(root/'app/src/main/java/com/maisonvie/game/engine/NativeGameBridge.kt').read_text()
for name in re.findall(r'Java_com_maisonvie_game_engine_NativeGameBridge_(\w+)',native):
    assert f'fun {name}(' in bridge, name
print('VERIFY OK:',vc,'vertices',ic,'indices',bc,'bones;',svc,'scene vertices',sic,'indices')
