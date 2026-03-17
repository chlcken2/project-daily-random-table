(function() {
  /**
   * ============================================
   * 홈페이지 JavaScript (home.js)
   * ============================================
   * 
   * [파일 역할]
   * - 홈페이지 메인 화면의 랭킹 시스템과 공개 레시피 표시
   * - "今日何食べる？(오늘의 식단)" 랭킹 표시
   * - "伝説のグルメ(명예의 식단)" 랭킹 표시
   * - "みんなの食卓(모두의 식탁)" 공개 레시피 그리드 표시
   * 
   * [동작 순서]
   * 1. 페이지 로드 완료 (DOMContentLoaded)
   * 2. loadTodayRanking() 호출 → API: /api/ranking/today?limit=5
   * 3. loadWeeklyRanking() 호출 → API: /api/ranking/weekly?limit=5
   * 4. loadPublicRecipes() 호출 → API: /api/recipes?type=publicAll
   * 5. 각 데이터를 받아 HTML 렌더링 → 화면 표시
   * 
   * [API 엔드포인트]
   * - GET /api/ranking/today?limit=5   : 오늘의 식단 랭킹 (당일 생성된 레시피 중 인기도 Top 5)
   * - GET /api/ranking/weekly?limit=5  : 명예의 식단 랭킹 (최근 7일간 인기도 Top 5)
   * - GET /api/recipes?type=publicAll  : 공개된 전체 레시피 목록 (최신순)
   * 
   * [인기도 계산식 - 백엔드에서 계산]
   * 인기도 점수 = (좋아요 수 × 3) + (댓글 수 × 2) + (조회수 × 1)
   * 예: 좋아요 2개 + 댓글 3개 + 조회수 5 = (2×3) + (3×2) + (5×1) = 17점
   * 
   * [인증]
   * - JWT 토큰을 localStorage에서 추출
   * - API 요청시 Authorization 헤더에 Bearer 토큰 포함 (선택적, 일부 API는 인증 불필요)
   */
  
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

  // API 통합: 랭킹 및 공개 레시피 로드
  function loadTodayRanking() {
    var token = localStorage.getItem('accessToken');
    fetch('/api/ranking/today?limit=5', {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    })
      .then(function(res) { return res.json(); })
      .then(function(data) {
        if (data.success && data.data) {
          renderTodayRanking(data.data);
        }
      })
      .catch(function(err) { console.error('今日の献立読み込みに失敗しました:', err); });
  }

  function renderTodayRanking(recipes) {
    var container = document.getElementById('today-ranking-container');
    if (!container || !recipes.length) return;
    
    container.innerHTML = recipes.slice(0, 5).map(function(r, i) {
      return '<a href="/recipe/detail/' + r.id + '" class="w-full flex items-center gap-3 p-3 rounded-lg hover:bg-amber-50 transition-colors text-left block">' +
        '<div class="flex-shrink-0 w-12 h-12 bg-gradient-to-br from-amber-400 to-orange-500 rounded-lg flex items-center justify-center text-white font-medium">' + (i+1) + '</div>' +
        '<div class="flex-1 min-w-0">' +
          '<h3 class="font-medium truncate">' + r.title + '</h3>' +
          '<div class="flex items-center gap-3 text-xs text-gray-500">' +
            '<span>❤ ' + r.likeCount + '</span>' +
            '<span>💬 ' + r.commentCount + '</span>' +
            '<span>👀 ' + r.viewCount + '</span>' +
          '</div>' +
        '</div>' +
      '</a>';
    }).join('');
  }

  function loadWeeklyRanking() {
    var token = localStorage.getItem('accessToken');
    fetch('/api/ranking/weekly?limit=5', {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    })
      .then(function(res) { return res.json(); })
      .then(function(data) {
        if (data.success && data.data) {
          renderWeeklyRanking(data.data);
        }
      })
      .catch(function(err) { console.error('名誉の献立読み込みに失敗しました:', err); });
  }

  function renderWeeklyRanking(recipes) {
    var container = document.getElementById('weekly-ranking-container');
    if (!container || !recipes.length) return;
    
    var rankColors = ['from-yellow-400 to-yellow-600', 'from-gray-300 to-gray-500', 'from-amber-600 to-amber-800', 'from-blue-400 to-blue-600', 'from-green-400 to-green-600'];
    container.innerHTML = recipes.slice(0, 5).map(function(r, i) {
      return '<a href="/recipe/detail/' + r.id + '" class="w-full flex items-center gap-3 p-3 rounded-lg hover:bg-yellow-50 transition-colors text-left block">' +
        '<div class="flex-shrink-0 w-12 h-12 bg-gradient-to-br ' + rankColors[i] + ' rounded-lg flex items-center justify-center text-white font-medium">' + (i+1) + '</div>' +
        '<div class="flex-1 min-w-0">' +
          '<h3 class="font-medium truncate">' + r.title + '</h3>' +
          '<div class="flex items-center gap-3 text-xs text-gray-500">' +
            '<span>❤ ' + r.likeCount + '</span>' +
            '<span>💬 ' + r.commentCount + '</span>' +
            '<span>👀 ' + r.viewCount + '</span>' +
          '</div>' +
        '</div>' +
      '</a>';
    }).join('');
  }

  function loadPublicRecipes() {
    var token = localStorage.getItem('accessToken');
    fetch('/api/recipes?type=publicAll', {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    })
      .then(function(res) { return res.json(); })
      .then(function(data) {
        if (data.success && data.data) {
          renderPublicRecipes(data.data);
        }
      })
      .catch(function(err) { console.error('公開中のレシピ読み込みに失敗しました:', err); });
  }

  function renderPublicRecipes(recipes) {
    var container = document.getElementById('tab-all');
    if (!container || !recipes.length) return;
    
    container.innerHTML = recipes.map(function(r) {
      return '<a href="/recipe/detail/' + r.id + '" class="recipe-card bg-white rounded-xl overflow-hidden shadow-md hover:shadow-xl transition-shadow text-left block border border-gray-100">' +
        '<div class="aspect-video bg-gray-200 overflow-hidden">' +
          '<img src="' + (r.titleImage || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400') + '" alt="' + r.title + '" class="w-full h-full object-cover" onerror="this.src=\'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400\'"/>' +
        '</div>' +
        '<div class="p-4">' +
          '<h3 class="font-medium mb-2">' + r.title + '</h3>' +
          '<p class="text-sm text-gray-600 line-clamp-2 mb-3">' + (r.description || '') + '</p>' +
          '<div class="flex items-center gap-2 mb-3">' +
            '<span class="px-2 py-1 bg-amber-100 text-amber-700 text-xs rounded">🔪 ' + (r.difficultyId == 1 ? '低' : r.difficultyId == 3 ? '高' : '中') + '</span>' +
            '<span class="text-xs text-gray-500">⏱ ' + (r.cookingTime || 20) + '分</span>' +
          '</div>' +
          '<div class="flex items-center justify-between mt-3 text-sm text-gray-500">' +
            '<div class="flex items-center gap-3">' +
              '<span>❤ ' + (r.likeCount || 0) + '</span>' +
              '<span>💬 ' + (r.commentCount || 0) + '</span>' +
              '<span>👀 ' + (r.viewCount || 0) + '</span>' +
            '</div>' +
            '<span class="text-xs">' + (r.createdAt ? r.createdAt.substring(0, 10) : '') + '</span>' +
          '</div>' +
        '</div>' +
      '</a>';
    }).join('');
  }

  // 페이지 로드 시 데이터 로드
  document.addEventListener('DOMContentLoaded', function() {
    loadTodayRanking();
    loadWeeklyRanking();
    loadPublicRecipes();
  });
})();
