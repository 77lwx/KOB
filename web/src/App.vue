<template>
  <NavBar />
  <router-view/>
</template>

<script>
import NavBar from './components/NavBar.vue'
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap/dist/js/bootstrap"
// import $ from 'jquery'

export default {
  components: { NavBar },
  comments:{
    NavBar
  },
  setup(){

    //  $.ajax({
    //    url: "http://127.0.0.1:3000/user/account/register/",
    //    type: "post",
    //    data: {
    //      username: "pjh",
    //      password: "31",
    //      confirmedPassword: "31",
    //    },
    //    success(resp) {
    //      console.log(resp);
    //    },
    //    error(resp) {
    //      console.log(resp);
    //    }
    //  })


  }
}


</script>


<style>
  body{
    background-image: url("F:/First_BigProject/web/src/assets/images/background.jpg");
    background-size: cover;
  }

</style>
