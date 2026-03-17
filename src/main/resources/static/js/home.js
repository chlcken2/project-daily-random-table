(function() {
  function getCookie(name) {
    var value = "; " + document.cookie;
    var parts = value.split("; " + name + "=");
    if (parts.length == 2) return parts.pop().split(";").shift();
  }

  function clearTokenCookies() {
    document.cookie = 'accessToken=; path=/; max-age=0';
    document.cookie = 'refreshToken=; path=/; max-age=0';
  }

  var authLink = document.getElementById('auth-link');
  var logoutBtn = document.getElementById('logout-btn');
  var accessToken = getCookie('accessToken');

  if (accessToken && logoutBtn && authLink) {
    authLink.classList.add('hidden');
    logoutBtn.classList.remove('hidden');
  }

  if (logoutBtn) {
    logoutBtn.addEventListener('click', function() {
      fetch('/auth/logout', { method: 'POST', credentials: 'same-origin' })
        .then(function() {
          clearTokenCookies();
          window.location.href = '/login';
        })
        .catch(function() {
          clearTokenCookies();
          window.location.href = '/login';
        });
    });
  }

  // API Integration: Load rankings and public recipes
  function loadTodayRanking() {
    var token = getCookie('accessToken');
    fetch('/api/ranking/today?limit=5', {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    })
      .then(function(res) { return res.json(); })
      .then(function(data) {
        if (data.success && data.data) {
          renderTodayRanking(data.data);
        }
      })
      .catch(function(err) { console.error('Failed to load today ranking:', err); });
  }

  function renderTodayRanking(recipes) {
    var container = document.getElementById('today-ranking-container');
    if (!container || !recipes.length) return;

    container.innerHTML = recipes.map(function(r, i) {
      return `<a href="/recipes/${r.id}" class="w-full flex items-center gap-3 p-3 rounded-lg hover:bg-amber-50 transition-colors text-left block">
        <div class="flex-shrink-0 w-12 h-12 bg-gradient-to-br from-amber-400 to-orange-500 rounded-lg flex items-center justify-center text-white font-medium">${i+1}</div>
        <div class="flex-1 min-w-0">
          <h3 class="font-medium truncate">${r.title}</h3>
          <div class="flex items-center gap-3 text-xs text-gray-500">
            <span>❤ ${r.likeCount}</span>
            <span>💬 ${r.commentCount}</span>
            <span>👀 ${r.viewCount}</span>
          </div>
        </div>
      </a>`;
    }).join('');
  }

  function loadWeeklyRanking() {
    var token = getCookie('accessToken');
    fetch('/api/ranking/weekly?limit=5', {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    })
      .then(function(res) { return res.json(); })
      .then(function(data) {
        if (data.success && data.data) {
          renderWeeklyRanking(data.data);
        }
      })
      .catch(function(err) { console.error('Failed to load weekly ranking:', err); });
  }

  function renderWeeklyRanking(recipes) {
    var container = document.getElementById('weekly-ranking-container');
    if (!container || !recipes.length) return;

    var rankColors = ['from-yellow-400 to-yellow-600', 'from-gray-300 to-gray-500', 'from-amber-600 to-amber-800'];
    container.innerHTML = recipes.slice(0, 3).map(function(r, i) {
      return `<a href="/recipes/${r.id}" class="w-full flex items-center gap-3 p-3 rounded-lg hover:bg-yellow-50 transition-colors text-left block">
        <div class="flex-shrink-0 w-12 h-12 bg-gradient-to-br ${rankColors[i]} rounded-lg flex items-center justify-center text-white font-medium">${i+1}</div>
        <div class="flex-1 min-w-0">
          <h3 class="font-medium truncate">${r.title}</h3>
          <div class="flex items-center gap-3 text-xs text-gray-500">
            <span>❤ ${r.likeCount}</span>
            <span>💬 ${r.commentCount}</span>
            <span>👀 ${r.viewCount}</span>
          </div>
        </div>
      </a>`;
    }).join('');
  }

  function loadPublicRecipes() {
    var token = getCookie('accessToken');
    fetch('/recipes?type=publicAll', {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    })
      .then(function(res) { return res.json(); })
      .then(function(data) {
        if (data.success && data.data) {
          renderPublicRecipes(data.data);
        }
      })
      .catch(function(err) { console.error('Failed to load public recipes:', err); });
  }

  function renderPublicRecipes(recipes) {
    var container = document.getElementById('tab-all');
    if (!container || !recipes.length) return;

    container.innerHTML = recipes.map(function(r) {
      return `<a href="/recipes/${r.id}" class="recipe-card bg-white rounded-xl overflow-hidden shadow-md hover:shadow-xl transition-shadow text-left block border border-gray-100">
        <div class="aspect-video bg-gray-200 overflow-hidden">
          <img src="${r.titleImage || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400'}" alt="${r.title}" class="w-full h-full object-cover" onerror="this.src='https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400'"/>
        </div>
        <div class="p-4">
          <h3 class="font-medium mb-2">${r.title}</h3>
          <p class="text-sm text-gray-600 line-clamp-2 mb-3">${r.description || ''}</p>
          <div class="flex items-center gap-2 mb-3">
            <span class="px-2 py-1 bg-amber-100 text-amber-700 text-xs rounded">🔪 ${r.difficultyId == 1 ? '低' : r.difficultyId == 3 ? '高' : '中'}</span>
            <span class="text-xs text-gray-500">⏱ ${r.cookingTime || 20}分</span>
          </div>
          <div class="flex items-center justify-between mt-3 text-sm text-gray-500">
            <div class="flex items-center gap-3">
              <span>❤ ${r.likeCount || 0}</span>
              <span>💬 ${r.commentCount || 0}</span>
              <span>👀 ${r.viewCount || 0}</span>
            </div>
            <span class="text-xs">${r.createdAt ? r.createdAt.substring(0, 10) : ''}</span>
          </div>
        </div>
      </a>`;
    }).join('');
  }

  // Load data on page load
  document.addEventListener('DOMContentLoaded', function() {
    loadTodayRanking();
    loadWeeklyRanking();
    loadPublicRecipes();
  });
})();