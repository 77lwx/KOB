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
                    <img src="https://img2.baidu.com/it/u=3134217149,4082921947&fm=253&fmt=auto&app=138&f=PNG?w=324&h=324"  style="position: absolute; left: -15px; top: 100px;">
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
            <div class="col-12" style="text-align: center; padding-top: 12vh;">
                <button @click="click_match_btn" type="button" class="btn btn-danger btn-lg">{{ match_btn_info }}</button>
            </div>
        </div>
    </div>
</template>

<script>
import {ref} from 'vue'
import { useStore } from 'vuex';

export default{
    setup() {
        let match_btn_info = ref("开始匹配");
        const store = useStore();

        const click_match_btn = () => {
            if (match_btn_info.value === "开始匹配") {
                match_btn_info.value = "取消";
                store.state.pk.socket.send(JSON.stringify({
                    event: "start-matching",
                }));
            } else {
                match_btn_info.value = "开始匹配";
                store.state.pk.socket.send(JSON.stringify({
                    event: "stop-matching",
                }));
            }
        }

        return {
            match_btn_info,
            click_match_btn,
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
    background-position: center top; /* 设置背景图位置为居中 */

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
</style>