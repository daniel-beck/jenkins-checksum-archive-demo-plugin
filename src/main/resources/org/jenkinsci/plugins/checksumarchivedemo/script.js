document.onreadystatechange = function(){
     if(document.readyState === 'complete'){
         document.getElementById('title').style.color = 'yellow';
         document.getElementById('description').style.color = 'yellow';
         document.body.style.backgroundColor = 'red';
     }
}