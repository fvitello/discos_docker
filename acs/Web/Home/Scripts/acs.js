var dayNames = new Array ();
dayNames[0]="Sunday";
dayNames[1]="Monday";
dayNames[2]="Tuesday";
dayNames[3]="Wednesday";
dayNames[4]="Thursday";
dayNames[5]="Friday";
dayNames[6]="Saturday";

var monthNames = new Array ();
monthNames[0]="January";
monthNames[1]="February";
monthNames[2]="March";
monthNames[3]="April";
monthNames[4]="May";
monthNames[5]="June";
monthNames[6]="July";
monthNames[7]="August";
monthNames[8]="September";
monthNames[9]="October";
monthNames[10]="November";
monthNames[11]="December";

var now = new Date();
var day = now.getDay();
var month = now.getMonth();
var year = now.getFullYear();
var date = now.getDate();


///////////////////////////////////////
//Script created by Jim Young (www.requestcode.com)
//Submitted to JavaScript Kit (http://javascriptkit.com)
//Visit http://javascriptkit.com for this script     
function showtip(current,e,tip)
{
 if (document.layers) // Netscape 4.0+
    {
     theString="<DIV CLASS='ttip'>"+tip+"</DIV>"
     document.tooltip.document.write(theString)
     document.tooltip.document.close()
     document.tooltip.left=e.pageX+14
     document.tooltip.top=e.pageY+2
     document.tooltip.visibility="show"
    }
 else
    {
     if(document.getElementById) // Netscape 6.0+ and Internet Explorer 5.0+
       {
         elm=document.getElementById("tooltip")
         elml=current
         elm.innerHTML=tip
         elm.style.height=elml.style.height
         elm.style.top=parseInt(elml.offsetTop+elml.offsetHeight)
         elm.style.left=parseInt(elml.offsetLeft+elml.offsetWidth+10)
         elm.style.visibility = "visible"
       }
    }
}

function hidetip()
{
  if (document.layers) // Netscape 4.0+
     {
      document.tooltip.visibility="hidden"
     }
  else
    {
     if(document.getElementById) // Netscape 6.0+ and Internet Explorer 5.0+
       {
        elm.style.visibility="hidden"
       }
    } 
}
//////////////////////////////////////