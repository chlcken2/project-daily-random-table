(function() {
  var ingredients = [];
  var sauces = [];
  var gachaCount = 0;
  var maxDaily = 3;
  var currentRecipeId = null;

  var ingredientList = document.getElementById('ingredient-list');
  var sauceList = document.getElementById('sauce-list');
  var gachaCountEl = document.getElementById('gacha-count');
  var ingredientCountEl = document.getElementById('ingredient-count');
  var sauceCountEl = document.getElementById('sauce-count');
  var gachaLimitMsg = document.getElementById('gacha-limit-msg');
  var gachaBtn = document.getElementById('gacha-btn');
  var gachaBtnText = document.getElementById('gacha-btn-text');
  var gachaWarning = document.getElementById('gacha-warning');
  var gachaRolling = document.getElementById('gacha-rolling');
  var gachaResult = document.getElementById('gacha-result');
  var progressBar = document.getElementById('progress-bar');
  var progressText = document.getElementById('progress-text');
  var errorToast = document.getElementById('error-toast');
  var errorToastMsg = document.getElementById('error-toast-msg');

  // Helper: escape HTML
  function esc(str) {
    if (!str) return '';
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
  }

  // ─────────────────────────────────────────────────────────────
  // ApiResponse 구조:
  //   { success, message, data, errorCode }
  //
  // /gacha/count   → data = DailyCountResponse { count, max, canGenerate }
  // /gacha/generate → data = GenerateResponse  { recipe: { id, title, ... } }
  // /gacha/publish  → data = null, message = "모두의 식탁에 등록되었습니다!"
  // ─────────────────────────────────────────────────────────────

  // Load daily count on page load
  function loadDailyCount() {
    fetch('/gacha/count')
      .then(function(r) { return r.json(); })
      .then(function(res) {
        if (!res.success) return;
        var d = res.data;  // DailyCountResponse
        gachaCount = d.count;
        maxDaily = d.max;
        gachaCountEl.textContent = gachaCount;
        if (!d.canGenerate) {
          gachaLimitMsg.classList.remove('hidden');
        }
        updateGachaBtn();
      })
      .catch(function() {});
  }

  function showError(msg) {
    errorToastMsg.textContent = msg;
    errorToast.classList.remove('hidden');
    setTimeout(function() { errorToast.classList.add('hidden'); }, 5000);
  }

  // Render ingredient list
  function renderIngredientList() {
    if (ingredients.length === 0) {
      ingredientList.innerHTML = '<p class="text-center text-gray-400 py-8">냉장고가 텅 비었어요 🥲</p>';
    } else {
      ingredientList.innerHTML = ingredients.map(function(item) {
        return '<div class="flex items-center justify-between bg-amber-50 p-3 rounded-lg">'
          + '<span>' + esc(item.name) + ' - ' + esc(item.amount) + item.unit + '</span>'
          + '<button type="button" class="remove-ingredient text-red-500 hover:text-red-700" data-id="' + item.id + '">'
          + '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>'
          + '</button></div>';
      }).join('');
      ingredientList.querySelectorAll('.remove-ingredient').forEach(function(btn) {
        btn.addEventListener('click', function() {
          ingredients = ingredients.filter(function(i) { return i.id !== btn.getAttribute('data-id'); });
          renderIngredientList();
          updateGachaBtn();
        });
      });
    }
    ingredientCountEl.textContent = ingredients.length + '개';
  }

  function renderSauceList() {
    if (sauces.length === 0) {
      sauceList.innerHTML = '<p class="text-center text-gray-400 py-8">양념통이 비었어요 (선택사항)</p>';
    } else {
      sauceList.innerHTML = sauces.map(function(item) {
        return '<div class="flex items-center justify-between bg-orange-50 p-3 rounded-lg">'
          + '<span>' + esc(item.name) + ' - ' + esc(item.amount) + item.unit + '</span>'
          + '<button type="button" class="remove-sauce text-red-500 hover:text-red-700" data-id="' + item.id + '">'
          + '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>'
          + '</button></div>';
      }).join('');
      sauceList.querySelectorAll('.remove-sauce').forEach(function(btn) {
        btn.addEventListener('click', function() {
          sauces = sauces.filter(function(s) { return s.id !== btn.getAttribute('data-id'); });
          renderSauceList();
        });
      });
    }
    sauceCountEl.textContent = sauces.length + '개';
  }

  function updateGachaBtn() {
    var canGen = gachaCount < maxDaily && ingredients.length > 0;
    gachaBtn.disabled = !canGen;
    if (ingredients.length === 0) {
      gachaWarning.classList.remove('hidden');
    } else {
      gachaWarning.classList.add('hidden');
    }
  }

  // Add ingredient
  document.getElementById('add-ingredient').addEventListener('click', function() {
    var name = document.getElementById('ingredient-name').value.trim();
    var amount = document.getElementById('ingredient-amount').value.trim();
    var unit = document.getElementById('ingredient-unit').value;
    if (name && amount) {
      ingredients.push({ id: Date.now().toString(), name: name, amount: amount, unit: unit });
      document.getElementById('ingredient-name').value = '';
      document.getElementById('ingredient-amount').value = '';
      renderIngredientList();
      updateGachaBtn();
    }
  });

  // Enter key for ingredient
  document.getElementById('ingredient-name').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') document.getElementById('add-ingredient').click();
  });

  // Add sauce
  document.getElementById('add-sauce').addEventListener('click', function() {
    var name = document.getElementById('sauce-name').value.trim();
    var amount = document.getElementById('sauce-amount').value.trim();
    var unit = document.getElementById('sauce-unit').value;
    if (name && amount) {
      sauces.push({ id: Date.now().toString(), name: name, amount: amount, unit: unit });
      document.getElementById('sauce-name').value = '';
      document.getElementById('sauce-amount').value = '';
      renderSauceList();
    }
  });

  // Enter key for sauce
  document.getElementById('sauce-name').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') document.getElementById('add-sauce').click();
  });

  // Progress animation
  function animateProgress() {
    var progress = 0;
    var messages = [
      'AI가 재료를 분석하고 있어요...',
      '최적의 레시피를 찾고 있어요...',
      '영양 정보를 계산하고 있어요...',
      '셰프가 마지막 손질 중...',
      '거의 다 됐어요!'
    ];
    var interval = setInterval(function() {
      progress += Math.random() * 15;
      if (progress > 90) progress = 90;
      progressBar.style.width = Math.min(progress, 90) + '%';
      var msgIdx = Math.min(Math.floor(progress / 20), messages.length - 1);
      progressText.textContent = messages[msgIdx];
    }, 800);
    return interval;
  }

  // GACHA GENERATE
  gachaBtn.addEventListener('click', function() {
    if (gachaCount >= maxDaily || ingredients.length === 0) return;

    gachaRolling.classList.remove('hidden');
    gachaResult.classList.add('hidden');
    gachaBtn.disabled = true;
    gachaBtnText.textContent = '🍳 요리 중...';
    progressBar.style.width = '0%';

    var progressInterval = animateProgress();

    var purpose = document.querySelector('input[name="purpose"]:checked').value;
    var cuisine = document.querySelector('input[name="cuisine"]:checked').value;
    var difficulty = document.querySelector('input[name="difficulty"]:checked').value;

    var requestBody = {
      ingredients: ingredients.map(function(i) { return { name: i.name, amount: i.amount, unit: i.unit }; }),
      sauces: sauces.map(function(s) { return { name: s.name, amount: s.amount, unit: s.unit }; }),
      purpose: purpose,
      cuisine: cuisine,
      difficulty: difficulty
    };

    fetch('/gacha/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      // res = ApiResponse { success, message, data: GenerateResponse { recipe }, errorCode }
      clearInterval(progressInterval);
      progressBar.style.width = '100%';

      setTimeout(function() {
        gachaRolling.classList.add('hidden');

        if (!res.success) {
          // GlobalExceptionHandler가 반환한 에러 (GACHA_DAILY_LIMIT, GACHA_AI_FAILURE 등)
          showError(res.message || '레시피 생성에 실패했습니다.');
          gachaBtn.disabled = false;
          gachaBtnText.textContent = '🎲 식단 가챠 돌리기!';
          return;
        }

        // Update count
        gachaCount++;
        gachaCountEl.textContent = gachaCount;
        if (gachaCount >= maxDaily) {
          gachaLimitMsg.classList.remove('hidden');
        }

        // res.data = GenerateResponse, res.data.recipe = RecipeResult
        var recipe = res.data.recipe;
        currentRecipeId = recipe.id;
        showResult(recipe);
        gachaBtn.disabled = gachaCount >= maxDaily || ingredients.length === 0;
        gachaBtnText.textContent = '🎲 식단 가챠 돌리기!';
      }, 500);
    })
    .catch(function(err) {
      clearInterval(progressInterval);
      gachaRolling.classList.add('hidden');
      showError('현재 내부 서비스의 지연으로 통신이 원활하지 않습니다. 새로고침 후 이용 부탁드립니다~');
      gachaBtn.disabled = false;
      gachaBtnText.textContent = '🎲 식단 가챠 돌리기!';
    });
  });

  function showResult(recipe) {
    gachaResult.classList.remove('hidden');

    document.getElementById('result-image').src = recipe.titleImage || '';
    document.getElementById('result-title').textContent = recipe.title || '';
    document.getElementById('result-summary').textContent = recipe.summary || '';
    document.getElementById('result-difficulty').textContent = '난이도: ' + (recipe.difficultyLabel || recipe.difficulty || '');
    document.getElementById('result-time-text').textContent = (recipe.cookingTime || 0) + '분';

    // Nutrients
    var nutrientsEl = document.getElementById('result-nutrients');
    nutrientsEl.innerHTML = '';
    if (recipe.nutrients) {
      var n = recipe.nutrients;
      var items = [
        { label: '칼로리', value: (n.calories || 0) + 'kcal', color: 'bg-red-50 text-red-700' },
        { label: '단백질', value: (n.protein || 0) + 'g', color: 'bg-blue-50 text-blue-700' },
        { label: '지방', value: (n.fat || 0) + 'g', color: 'bg-yellow-50 text-yellow-700' },
        { label: '탄수화물', value: (n.carbs || 0) + 'g', color: 'bg-green-50 text-green-700' }
      ];
      items.forEach(function(item) {
        nutrientsEl.innerHTML += '<div class="' + item.color + ' p-2 rounded-lg text-center"><div class="text-xs">' + item.label + '</div><div class="font-bold text-sm">' + item.value + '</div></div>';
      });
    }

    // Ingredients
    var ingListEl = document.getElementById('result-ingredients-list');
    ingListEl.innerHTML = '';
    if (recipe.ingredients) {
      recipe.ingredients.forEach(function(ing) {
        ingListEl.innerHTML += '<span class="px-2 py-1 bg-amber-100 text-amber-800 rounded-full text-xs">' + esc(ing.name) + ' ' + (ing.amount || '') + (ing.unit || '') + '</span>';
      });
    }

    // Detail link
    document.getElementById('btn-detail').href = '/gacha/recipe/' + recipe.id;

    // Scroll to result
    gachaResult.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  // Private save (나만의 식탁)
  // res = ApiResponse { success, message, data:null }
  document.getElementById('btn-private').addEventListener('click', function() {
    if (!currentRecipeId) return;
    fetch('/gacha/publish/' + currentRecipeId + '?isPublic=false', { method: 'POST' })
      .then(function(r) { return r.json(); })
      .then(function(res) {
        if (res.success) {
          window.location.href = '/mypage';
        } else {
          showError(res.message);
        }
      })
      .catch(function() { showError('저장에 실패했습니다.'); });
  });

  // Public save (모두의 식탁)
  document.getElementById('btn-public').addEventListener('click', function() {
    if (!currentRecipeId) return;
    fetch('/gacha/publish/' + currentRecipeId + '?isPublic=true', { method: 'POST' })
      .then(function(r) { return r.json(); })
      .then(function(res) {
        if (res.success) {
          alert('모두의 식탁에 등록되었습니다! 🎉');
        } else {
          showError(res.message);
        }
      })
      .catch(function() { showError('등록에 실패했습니다.'); });
  });

  // Init
  renderIngredientList();
  renderSauceList();
  updateGachaBtn();
  loadDailyCount();
})();
