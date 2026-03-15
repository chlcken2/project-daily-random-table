(function() {
  var activeTab = 'recipes';
  var viewMode = 'grid';

  var accessToken = localStorage.getItem('accessToken');
  if (!accessToken) {
    window.location.href = '/login';
    return;
  }

  fetch('/users/me', {
    headers: { 'Authorization': 'Bearer ' + accessToken }
  })
    .then(function(res) {
      if (res.status === 401) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        return null;
      }
      return res.json();
    })
    .then(function(result) {
      if (!result) return;
      if (result.success && result.data) {
        var d = result.data;
        var nickEl = document.getElementById('profile-nickname');
        var emailEl = document.getElementById('profile-email');
        var roleEl = document.getElementById('profile-role');
        var myRecipeEl = document.getElementById('profile-my-recipe-count');
        var receivedLikeEl = document.getElementById('profile-received-like-count');
        var gachaEl = document.getElementById('profile-gacha-count');
        if (nickEl) nickEl.textContent = d.nickname || '—';
        if (emailEl) emailEl.textContent = d.email || '—';
        if (roleEl && d.role) roleEl.textContent = d.role;
        if (myRecipeEl) myRecipeEl.textContent = typeof d.myRecipeCount === 'number' ? d.myRecipeCount : 0;
        if (receivedLikeEl) receivedLikeEl.textContent = typeof d.receivedLikeCount === 'number' ? d.receivedLikeCount : 0;
        if (gachaEl) gachaEl.textContent = typeof d.totalGachaCount === 'number' ? d.totalGachaCount : 0;
      }
    })
    .catch(function() {
      var nickEl = document.getElementById('profile-nickname');
      var emailEl = document.getElementById('profile-email');
      var myRecipeEl = document.getElementById('profile-my-recipe-count');
      var receivedLikeEl = document.getElementById('profile-received-like-count');
      var gachaEl = document.getElementById('profile-gacha-count');
      if (nickEl) nickEl.textContent = '—';
      if (emailEl) emailEl.textContent = '—';
      if (myRecipeEl) myRecipeEl.textContent = '0';
      if (receivedLikeEl) receivedLikeEl.textContent = '0';
      if (gachaEl) gachaEl.textContent = '0';
    });

  var tabContentMap = {
    recipes: { grid: 'view-grid', list: 'view-list' },
    liked: { grid: 'view-liked-grid', list: 'view-liked-list' }
  };

  function showTab(tab) {
    activeTab = tab;
    document.querySelectorAll('.mypage-tab-btn').forEach(function(btn) {
      if (btn.getAttribute('data-tab') === tab) {
        btn.classList.remove('mypage-tab-inactive');
        btn.classList.add('mypage-tab-active');
      } else {
        btn.classList.remove('mypage-tab-active');
        btn.classList.add('mypage-tab-inactive');
      }
    });
    updateView();
  }

  function updateView() {
    var content = tabContentMap[activeTab];
    if (!content) return;
    document.querySelectorAll('[id^="view-"]').forEach(function(el) {
      el.classList.add('hidden');
    });
    var targetId = viewMode === 'grid' ? content.grid : content.list;
    var target = document.getElementById(targetId);
    if (target) target.classList.remove('hidden');
  }

  document.querySelectorAll('.mypage-tab-btn').forEach(function(btn) {
    btn.addEventListener('click', function() {
      showTab(btn.getAttribute('data-tab'));
    });
  });

  document.querySelectorAll('.view-btn').forEach(function(btn) {
    btn.addEventListener('click', function() {
      viewMode = btn.getAttribute('data-view');
      document.querySelectorAll('.view-btn').forEach(function(b) {
        b.classList.remove('view-active');
        b.classList.add('view-inactive');
      });
      btn.classList.remove('view-inactive');
      btn.classList.add('view-active');
      updateView();
    });
  });

  var logoutBtn = document.getElementById('mypage-logout');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', function() {
      var rt = localStorage.getItem('refreshToken');
      var opts = { method: 'POST', headers: {} };
      if (rt) opts.headers['Authorization'] = 'Bearer ' + rt;
      fetch('/auth/logout', opts).then(function() {}).catch(function() {});
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      window.location.href = '/login';
    });
  }
})();
