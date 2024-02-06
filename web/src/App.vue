<template>
  <NavBar />
  <router-view/>
</template>

<script>
import NavBar from './components/NavBar.vue'
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap/dist/js/bootstrap"

export default {
  components: { NavBar },
  comments:{
    NavBar
  }
}


</script>


<style>
  body{
    background-image: url("F:/First_BigProject/web/src/assets/images/background.jpg");
    background-size: cover;
  }

</style>
