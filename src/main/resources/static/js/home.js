(function() {
  document.querySelectorAll('.tab-btn').forEach(function(btn) {
    btn.addEventListener('click', function() {
      var tab = btn.getAttribute('data-tab');
      document.querySelectorAll('.tab-btn').forEach(function(b) {
        b.classList.remove('tab-active');
        b.classList.add('tab-inactive');
      });
      btn.classList.remove('tab-inactive');
      btn.classList.add('tab-active');

      document.querySelectorAll('.tab-content').forEach(function(c) {
        c.classList.add('hidden');
      });
      var target = document.getElementById('tab-' + tab);
      if (target) target.classList.remove('hidden');
    });
  });
})();
