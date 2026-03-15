(function() {
  var activeTab = 'recipes';

  var accessToken = localStorage.getItem('accessToken');
  if (!accessToken) {
    window.location.href = '/login';
    return;
  }

  var headers = { 'Authorization': 'Bearer ' + accessToken };

  fetch('/users/me', { headers: headers })
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

  function escapeHtml(s) {
    if (!s) return '';
    return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/"/g, '&quot;');
  }

  function renderMyRecipeCard(item) {
    var img = (item.titleImage && item.titleImage.trim()) ? item.titleImage : null;
    var title = escapeHtml((item.title && item.title.trim()) ? item.title : '無題');
    var imgHtml = img
      ? '<img src="' + escapeHtml(img) + '" alt="" class="w-full h-full object-cover" onerror="this.parentElement.classList.add(\'bg-gray-200\');this.remove()"/>'
      : '';
    return '<a href="/gacha/recipe/' + item.id + '" class="block rounded-lg overflow-hidden bg-gray-100 hover:opacity-75 transition-opacity">' +
      '<div class="aspect-square overflow-hidden">' + imgHtml + '</div>' +
      '<div class="p-2 text-sm font-medium text-gray-700 truncate">' + title + '</div>' +
      '</a>';
  }

  function renderLikedRecipeCard(item) {
    var img = (item.titleImage && item.titleImage.trim()) ? item.titleImage : null;
    var title = escapeHtml((item.title && item.title.trim()) ? item.title : '無題');
    var imgHtml = img
      ? '<img src="' + escapeHtml(img) + '" alt="" class="w-full h-full object-cover" onerror="this.parentElement.classList.add(\'bg-gray-200\');this.remove()"/>'
      : '';
    var sub = [];
    if (item.cookingTime != null) sub.push(item.cookingTime + '分');
    if (item.difficultyName) sub.push(escapeHtml(item.difficultyName));
    if (item.likeCount != null) sub.push('❤ ' + item.likeCount);
    var subText = sub.length ? sub.join(' · ') : '';
    return '<a href="/gacha/recipe/' + item.id + '" class="block rounded-lg overflow-hidden bg-gray-50 hover:bg-gray-100 transition-colors">' +
      '<div class="aspect-square bg-gray-200 overflow-hidden">' + imgHtml + '</div>' +
      '<div class="p-2">' +
      '<h3 class="font-medium text-sm text-gray-800 truncate">' + title + '</h3>' +
      (subText ? '<p class="text-xs text-gray-500 mt-0.5">' + subText + '</p>' : '') +
      '</div></a>';
  }

  function renderGrid(containerId, list, renderCard, emptyMessage) {
    var el = document.getElementById(containerId);
    if (!el) return;
    if (!list || list.length === 0) {
      el.innerHTML = '<p class="col-span-3 text-center text-gray-500 py-8">' + (emptyMessage || 'まだありません') + '</p>';
      return;
    }
    el.innerHTML = list.map(renderCard).join('');
  }

  function loadRecipes() {
    fetch('/users/me/recipes?type=my', { headers: headers })
      .then(function(res) { return res.status === 401 ? null : res.json(); })
      .then(function(result) {
        if (result && result.success && Array.isArray(result.data)) {
          renderGrid('my-recipes-grid', result.data, renderMyRecipeCard, 'マイレシピはまだありません');
        } else {
          renderGrid('my-recipes-grid', [], renderMyRecipeCard, 'マイレシピはまだありません');
        }
      })
      .catch(function() {
        renderGrid('my-recipes-grid', [], renderMyRecipeCard, 'マイレシピはまだありません');
      });

    fetch('/users/me/recipes?type=liked', { headers: headers })
      .then(function(res) { return res.status === 401 ? null : res.json(); })
      .then(function(result) {
        if (result && result.success && Array.isArray(result.data)) {
          renderGrid('liked-recipes-grid', result.data, renderLikedRecipeCard, 'いいねしたレシピはまだありません');
        } else {
          renderGrid('liked-recipes-grid', [], renderLikedRecipeCard, 'いいねしたレシピはまだありません');
        }
      })
      .catch(function() {
        renderGrid('liked-recipes-grid', [], renderLikedRecipeCard, 'いいねしたレシピはまだありません');
      });
  }

  loadRecipes();

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
    var myGrid = document.getElementById('my-recipes-grid');
    var likedGrid = document.getElementById('liked-recipes-grid');
    if (tab === 'recipes') {
      if (myGrid) myGrid.classList.remove('hidden');
      if (likedGrid) likedGrid.classList.add('hidden');
    } else {
      if (myGrid) myGrid.classList.add('hidden');
      if (likedGrid) likedGrid.classList.remove('hidden');
    }
  }

  document.querySelectorAll('.mypage-tab-btn').forEach(function(btn) {
    btn.addEventListener('click', function() {
      showTab(btn.getAttribute('data-tab'));
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
