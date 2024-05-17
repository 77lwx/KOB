<template>
  <div ref="parent" class="gamemap">

        <div class="text-box">  
            <h2 class="box-title">玩家：{{ $store.state.user.username  }}</h2>  
            <h2 class="box-title" v-if="$store.state.user.id == $store.state.pk.a_id">颜色:蓝色</h2>  
            <h2 class="box-title" v-if="$store.state.user.id == $store.state.pk.b_id">颜色:红色</h2> 
        </div>  

        <canvas ref="canvas" tabindex="0"></canvas>
  </div>
</template>

<script>
import{ GameMap } from "../assets/scripts/GameMap";
import {ref , onMounted} from 'vue'
import { useStore } from "vuex";
export default{
    setup(){
        let parent=ref(null);
        let canvas=ref(null);
        const store=useStore();

        onMounted(()=>{
            store.commit(
                "updateGameObject",
                new GameMap(canvas.value.getContext('2d'),parent.value,store),
            );

        });



        return {
            parent,
            canvas,
        }
    }
}

</script>

<style scoped>
div.gamemap{
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;

}

.text-box {  
    padding: 5px; /* 内边距 */  
    margin-bottom: 20px; /* 下边距，用于分隔两个框 */  
    position: relative;
    top:20px;
    right: 60px;
}  

.box-title {  
    margin: 0 0 10px 0; /* 上下边距，与内容分开 */  
    font-size: 40px; /* 字体大小 */  
    font-weight: bold; /* 加粗 */  
    text-shadow: 2px 2px 4px rgba(14, 116, 60, 0.5); /* 向右下偏移2px，模糊半径为4px的黑色阴影，透明度为0.5 */  
    text-decoration: none; /* 移除下划线（默认是none） */
}


  


</style>

