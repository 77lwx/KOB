<template>
    <div class="matchground">
        <div class="row">
            <div class="col-4">
                <div class="user-photo">
                    <img :src="$store.state.user.photo" alt="">
                </div>
                <div class="user-username">
                   {{ $store.state.user.username }}
                </div>
            </div>
            <div class="col-4">
                <div class="vs" style="position: relative;">
                    <img src="https://img2.baidu.com/it/u=3134217149,4082921947&fm=253&fmt=auto&app=138&f=PNG?w=324&h=324"  style="position: absolute; left: -15px; top: 70px;">
                </div>
                <div class="user-select-bot">
                    <select class="form-select" aria-label="Default select example" v-model="select_bot"> <!--双向绑定bot-->
                        <option value="-1" selected>亲自出马</option>
                        <option v-for="bot in bots" :key="bot.id" :value="bot.id">{{ bot.title }}</option>
                    </select>
                </div>
            </div>
            <div class="col-4">
                <div class="user-photo">
                    <img :src="$store.state.pk.opponent_photo" alt="">
                </div>
                <div class="user-username">
                    {{ $store.state.pk.opponent_username }}
                </div>
            </div>
            <div class="col-12" style="text-align: center; padding-top: 5vh;">
                <button @click="click_match_btn" type="button" class="btn btn-danger btn-lg">{{ match_btn_info }}</button>
            </div>
        </div>
    </div>
</template>

<script>
import {ref} from 'vue'
import { useStore } from 'vuex';
import $ from 'jquery';

export default{
    setup() {
        let match_btn_info = ref("开始匹配");
        const store = useStore();

        let bots = ref([]);
        let select_bot = ref("-1");//前端选择的bot_id  -1代表玩家亲自游戏
        

        const click_match_btn = () => {
            if (match_btn_info.value === "开始匹配") {
                console.log(select_bot.value);
                match_btn_info.value = "取消";
                store.state.pk.socket.send(JSON.stringify({
                    event: "start-matching",
                    bot_id: select_bot.value,
                }));
            } else {
                match_btn_info.value = "开始匹配";
                store.state.pk.socket.send(JSON.stringify({
                    event: "stop-matching",
                }));
            }
        }

        const refresh_bots = () => {
            $.ajax({
                url: "http://127.0.0.1:3000/user/bot/getlist/",
                type: "get",
                headers: {
                    'Authorization': "Bearer " + store.state.user.token,
                },
                success(resp) {
                    bots.value = resp;
                }
            })
        };
        refresh_bots();



        return {
            match_btn_info,
            click_match_btn,
            refresh_bots,
            select_bot,
            bots,
        }
    }
}
</script>
<style scoped>
div.matchground {
    width: 60vw;
    height: 70vh;
    margin: 40px auto;
    background-image: url('https://img2.huashi6.com/images/resource/thumbnail/2023/10/14/225827_50727032447.jpg?imageMogr2/quality/75/interlace/1/thumbnail/x1400/gravity/Center/crop/1400x1400/format/webp');
    background-size: cover; /* 调整背景图大小以覆盖整个容器 */
    background-position: center top; /* 设置背景图位置为水平居中，垂直顶部 */
    border: 2px solid #000; 
    border-radius: 10px; /* 添加圆角边框效果 */
}



div.user-photo {
    text-align: center;
    padding-top: 18vh;

}

div.user-photo > img {
    border-radius: 50%;
    width: 20vh;
}



div.user-username {
    text-align: center;
    font-size: 24px;
    font-weight: 600;
    color: white;
    padding-top: 2vh;
}


div.user-select-bot {
    padding-top: 50vh;
}
div.user-select-bot > select {
    width: 60%;
    margin: 0 auto;
}
</style>