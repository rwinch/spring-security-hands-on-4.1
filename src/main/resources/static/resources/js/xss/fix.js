$("#p").click(function() {
	var message = $(this).attr("data-bind");
	process(message);
})

function process(a) {
	alert('Done Processing: ' + a);
}