<template>
    <PlayGround v-if="$store.state.pk.status === 'playing'" />
    <MatchGround v-if="$store.state.pk.status === 'matching'" />
</template>

<script>
import PlayGround from '../../components/PlayGround.vue'
import MatchGround from '../../components/MatchGround.vue'
import{ onMounted , onUnmounted} from 'vue'
import{useStore} from 'vuex'


export default{
    components:{
        PlayGround,
        MatchGround,
    },
    setup(){
        const store = useStore();
        const socketUrl=`ws://127.0.0.1:3000/websocket/${store.state.user.token}/`;


        let socket = null;
        store.commit("updateOpponent", {
                username: "我的对手",
                photo: "https://img0.baidu.com/it/u=3490576489,1953736046&fm=253&fmt=auto&app=138&f=JPEG?w=200&h=200",
            })

        onMounted(()=>{
            socket=new WebSocket(socketUrl);

            socket.onopen = () => {
                console.log("connected!");
                store.commit("updateSocket", socket);
            }


            socket.onmessage = msg => {
                const data = JSON.parse(msg.data);
                 if (data.event === "start-matching") {  // 匹配成功
                    store.commit("updateOpponent", {
                        username: data.opponent_username,
                        photo: data.opponent_photo,
                    });
                    setTimeout(() => {
                        store.commit("updateStatus", "playing");
                    }, 2000);
                    store.commit("updateGamemap",data.gamemap);
                }

            }


            socket.onclose = () => {
                console.log("disconnected!");
                store.commit("updateStatus", "matching");
            }
        });


        onUnmounted(() => {
            socket.close();
        })

    }

}
</script>


<style scoped>

</style>