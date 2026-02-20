(function() {
  var ingredients = [];
  var sauces = [];
  var gachaCount = 0;
  var maxDaily = 3;

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
  var resultTitle = document.getElementById('result-title');
  var resultDesc = document.getElementById('result-desc');

  function renderIngredientList() {
    if (ingredients.length === 0) {
      ingredientList.innerHTML = '<p class="text-center text-gray-400 py-8">냉장고가 텅 비었어요 🥲</p>';
    } else {
      ingredientList.innerHTML = ingredients.map(function(item) {
        var safeName = (item.name || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
        var safeAmount = (item.amount || '').replace(/&/g, '&amp;').replace(/</g, '&lt;');
        return '<div class="flex items-center justify-between bg-amber-50 p-3 rounded-lg"><span>' + safeName + ' - ' + safeAmount + (item.unit || '') + '</span><button type="button" class="remove-ingredient text-red-500 hover:text-red-700" data-id="' + item.id + '"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>';
      }).join('');
      ingredientList.querySelectorAll('.remove-ingredient').forEach(function(btn) {
        btn.addEventListener('click', function() {
          var id = btn.getAttribute('data-id');
          ingredients = ingredients.filter(function(i) { return i.id !== id; });
          renderIngredientList();
          ingredientCountEl.textContent = ingredients.length + '개';
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
        var safeName = (item.name || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
        var safeAmount = (item.amount || '').replace(/&/g, '&amp;').replace(/</g, '&lt;');
        return '<div class="flex items-center justify-between bg-orange-50 p-3 rounded-lg"><span>' + safeName + ' - ' + safeAmount + (item.unit || '') + '</span><button type="button" class="remove-sauce text-red-500 hover:text-red-700" data-id="' + item.id + '"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>';
      }).join('');
      sauceList.querySelectorAll('.remove-sauce').forEach(function(btn) {
        btn.addEventListener('click', function() {
          var id = btn.getAttribute('data-id');
          sauces = sauces.filter(function(s) { return s.id !== id; });
          renderSauceList();
          sauceCountEl.textContent = sauces.length + '개';
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

  gachaBtn.addEventListener('click', function() {
    if (gachaCount >= maxDaily || ingredients.length === 0) return;
    gachaRolling.classList.remove('hidden');
    gachaResult.classList.add('hidden');
    gachaBtn.disabled = true;
    gachaBtnText.textContent = '🎰 가챠 돌리는 중...';
    setTimeout(function() {
      gachaCount++;
      gachaCountEl.textContent = gachaCount;
      gachaRolling.classList.add('hidden');
      gachaResult.classList.remove('hidden');
      resultTitle.textContent = (ingredients[0] ? ingredients[0].name : '재료') + ' 볶음';
      resultDesc.textContent = ingredients.map(function(i) { return i.name; }).join(', ') + '로 만드는 간단하고 맛있는 요리. 속세의맛에 딱 맞는 레시피입니다!';
      gachaBtn.disabled = gachaCount >= maxDaily || ingredients.length === 0;
      gachaBtnText.textContent = '🎲 식단 가챠 돌리기!';
      if (gachaCount >= maxDaily) {
        gachaLimitMsg.classList.remove('hidden');
      }
    }, 2500);
  });

  renderIngredientList();
  renderSauceList();
  updateGachaBtn();
})();
