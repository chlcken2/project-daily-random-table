(function() {
  var authLink = document.getElementById('auth-link');
  var logoutBtn = document.getElementById('logout-btn');
  var accessToken = localStorage.getItem('accessToken');

  if (accessToken && logoutBtn && authLink) {
    authLink.classList.add('hidden');
    logoutBtn.classList.remove('hidden');
  }

  if (logoutBtn) {
    logoutBtn.addEventListener('click', function() {
      var rt = localStorage.getItem('refreshToken');
      var opts = { method: 'POST', headers: {} };
      if (rt) opts.headers['Authorization'] = 'Bearer ' + rt;
      fetch('/auth/logout', opts)
        .then(function() {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
        })
        .catch(function() {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
        });
    });
  }
})();
