// 쿠키 조회 유틸리티
function getCookie(name) {
  var nameEQ = name + "=";
  var ca = document.cookie.split(';');
  for(var i=0; i < ca.length; i++) {
    var c = ca[i];
    while (c.charAt(0)==' ') c = c.substring(1,c.length);
    if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
  }
  return null;
}

(function() {
  var ingredients = [];
  var sauces = [];
  var gachaCount = 0;
  var maxDaily = 3;
  var currentRecipeId = null;

  var ingredientList    = document.getElementById('ingredient-list');
  var sauceList         = document.getElementById('sauce-list');
  var gachaCountEl      = document.getElementById('gacha-count');
  var ingredientCountEl = document.getElementById('ingredient-count');
  var sauceCountEl      = document.getElementById('sauce-count');
  var gachaLimitMsg     = document.getElementById('gacha-limit-msg');
  var gachaBtn          = document.getElementById('gacha-btn');
  var gachaBtnText      = document.getElementById('gacha-btn-text');
  var gachaWarning      = document.getElementById('gacha-warning');
  var gachaResult       = document.getElementById('gacha-result');
  var errorToast        = document.getElementById('error-toast');
  var errorToastMsg     = document.getElementById('error-toast-msg');

  function esc(str) {
    if (str == null) return '';
    if (typeof str !== 'string') str = String(str);
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
  }

  // ═══════════════════════════════════════════════════════════════
  //  입력 검증 모듈
  // ═══════════════════════════════════════════════════════════════
  // 한국어/영어/일본어/한자 및 공백만 허용 (숫자/특수문자/기호 차단)
  var VALID_NAME_REGEX = /^[가-힣ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z\u3040-\u30FF\\u4E00-\\u9FAF\\s]+$/;
  // 숫자만 허용 (분량 필드)
  var VALID_AMOUNT_REGEX = /^[0-9]*$/;

  // 마지막 오류 표시 시간 (중복 토스트 방지용)
  var lastErrorTime = 0;
  var ERROR_COOLDOWN = 2000; // 2초 쿨다운

  function showInputError(message) {
    var now = Date.now();
    if (now - lastErrorTime < ERROR_COOLDOWN) return; // 쿨다운 중이면 무시
    lastErrorTime = now;
    showError(message);
  }

  function blockInvalidKeys(input, regex, errorMessage) {
    // beforeinput: 최신 브라우저에서 입력 직전 차단
    input.addEventListener('beforeinput', function(e) {
      // 삭제, 실행 취소 등 데이터가 없는 입력 타입은 허용
      if (e.data === null || e.inputType === 'deleteContentBackward' || e.inputType === 'deleteContentForward') return;

      if (!regex.test(e.data)) {
        e.preventDefault();
        if (errorMessage) showInputError(errorMessage);
      }
    });

    // keydown: 모든 브라우저 호환용 (백스페이스/삭제/화살표 등 허용)
    input.addEventListener('keydown', function(e) {
      // 허용: 기능키 (백스페이스, 삭제, 화살표, 탭, 엔터, ESC)
      var allowedKeys = ['Backspace','Delete','ArrowLeft','ArrowRight','ArrowUp','ArrowDown','Tab','Enter','Escape'];
      // 허용: 단축키 (Ctrl, Alt, Meta 조합)
      if (e.ctrlKey || e.altKey || e.metaKey) return;
      // 허용: 기능키 - 조용히 허용 (오류 메시지 없음)
      if (allowedKeys.indexOf(e.key) !== -1) {
        return;
      }

      // 차단: 숫자만 허용하는 경우 숫자가 아니면 차단
      if (regex.source === '^[0-9]*$') {
        if (e.key < '0' || e.key > '9') {
          e.preventDefault();
          if (errorMessage) showInputError(errorMessage);
          return false;
        }
        return;
      }

      // 차단: 숫자 (0-9) - 이름 필드용
      if (e.key >= '0' && e.key <= '9') {
        e.preventDefault();
        if (errorMessage) showInputError(errorMessage);
        return false;
      }

      // 차단: 특수문자 (한글/영어/일본어/한자/공백 외 모든 문자)
      // @, #, $, % 등 모든 특수문자 차단
      if (!regex.test(e.key)) {
        e.preventDefault();
        if (errorMessage) showInputError(errorMessage);
        return false;
      }
    });

    input.addEventListener('paste', function(e) {
      e.preventDefault();
      if (errorMessage) showInputError(errorMessage);
    });
  }

  function setupValidation() {
    var ingredientName = document.getElementById('ingredient-name');
    var sauceName = document.getElementById('sauce-name');
    var ingredientAmount = document.getElementById('ingredient-amount');
    var sauceAmount = document.getElementById('sauce-amount');

    if (ingredientName) blockInvalidKeys(ingredientName, VALID_NAME_REGEX, '特殊文字と数字は入力できません');
    if (sauceName) blockInvalidKeys(sauceName, VALID_NAME_REGEX, '特殊文字と数字は入力できません');

    // 분량 필드: 숫자만 허용
    if (ingredientAmount) {
      // 조합 입력 차단 (한글/일본어/한자 입력기)
      ingredientAmount.addEventListener('compositionstart', function(e) {
        e.preventDefault();
        return false;
      });

      // keydown: 키 누르는 순간 차단 (가장 빠른 차단)
      ingredientAmount.addEventListener('keydown', function(e) {
        // 허용: 숫자(0-9), 백스페이스, 삭제, 화살표, 탭, 엔터
        var allowedKeys = ['Backspace','Delete','ArrowLeft','ArrowRight','Tab','Enter'];
        var isNumber = (e.key >= '0' && e.key <= '9');
        var isAllowedKey = allowedKeys.indexOf(e.key) !== -1;

        if (!isNumber && !isAllowedKey) {
          e.preventDefault();
          showInputError('数字のみ入力可能です');
          return false;
        }
      });

      // input: 혹시라도 통과한 경우 최종 필터링
      ingredientAmount.addEventListener('input', function(e) {
        var value = ingredientAmount.value;
        // 숫자가 아닌 것 모두 제거
        var filtered = value.replace(/[^0-9]/g, '');
        if (value !== filtered) {
          ingredientAmount.value = filtered;
        }
      });

      // 붙여넣기 완전 차단
      ingredientAmount.addEventListener('paste', function(e) {
        e.preventDefault();
        showInputError('数字のみ入力可能です');
        return false;
      });
    }

    if (sauceAmount) {
      // 조합 입력 차단 (한글/일본어/한자 입력기)
      sauceAmount.addEventListener('compositionstart', function(e) {
        e.preventDefault();
        return false;
      });

      // keydown: 키 누르는 순간 차단
      sauceAmount.addEventListener('keydown', function(e) {
        var allowedKeys = ['Backspace','Delete','ArrowLeft','ArrowRight','Tab','Enter'];
        var isNumber = (e.key >= '0' && e.key <= '9');
        var isAllowedKey = allowedKeys.indexOf(e.key) !== -1;

        if (!isNumber && !isAllowedKey) {
          e.preventDefault();
          showInputError('数字のみ入力可能です');
          return false;
        }
      });

      // input: 최종 필터링
      sauceAmount.addEventListener('input', function(e) {
        var value = sauceAmount.value;
        var filtered = value.replace(/[^0-9]/g, '');
        if (value !== filtered) {
          sauceAmount.value = filtered;
        }
      });

      sauceAmount.addEventListener('paste', function(e) {
        e.preventDefault();
        showInputError('数字のみ入力可能です');
        return false;
      });
    }
  }

  // ═══════════════════════════════════════════════════════════════
  //  가챠 머신 모듈
  // ═══════════════════════════════════════════════════════════════
  var GachaMachine = (function() {
    var overlay      = document.getElementById('gacha-overlay');
    var phaseMsg     = document.getElementById('gacha-phase-msg');
    var lcdEl        = document.getElementById('gacha-lcd');
    var spinner      = document.getElementById('capsule-spinner');
    var dialEl       = document.getElementById('gacha-dial');
    var coinDisplay  = document.getElementById('coin-display');
    var particles    = document.getElementById('gacha-particles');
    var outputIdle   = document.getElementById('output-idle');
    var capsuleOut   = document.getElementById('capsule-result');
    var machineBody  = document.getElementById('gacha-machine-body');

    var musicRef   = null;
    var timers     = [];

    // ── Web Audio 8-bit BGM (Twinkle Twinkle - Public Domain) ──
    function startMusic() {
      var AC = window.AudioContext || window.webkitAudioContext;
      if (!AC) return null;
      try {
        var ctx = new AC();
        var master = ctx.createGain();
        master.gain.setValueAtTime(0.07, ctx.currentTime);
        master.connect(ctx.destination);
        var looping = true;

        var bpm    = 152;
        var beat   = 60 / bpm;
        // Twinkle Twinkle Little Star – public domain melody
        // [frequency_hz, duration_beats]
        var mel = [
          [523.25,0.5],[523.25,0.5],[784.00,0.5],[784.00,0.5],
          [880.00,0.5],[880.00,0.5],[784.00,1.0],
          [698.46,0.5],[698.46,0.5],[659.25,0.5],[659.25,0.5],
          [587.33,0.5],[587.33,0.5],[523.25,1.0],
          [784.00,0.5],[784.00,0.5],[698.46,0.5],[698.46,0.5],
          [659.25,0.5],[659.25,0.5],[587.33,1.0],
          [784.00,0.5],[784.00,0.5],[698.46,0.5],[698.46,0.5],
          [659.25,0.5],[659.25,0.5],[587.33,1.0],
          [523.25,0.5],[523.25,0.5],[784.00,0.5],[784.00,0.5],
          [880.00,0.5],[880.00,0.5],[784.00,1.0],
          [698.46,0.5],[698.46,0.5],[659.25,0.5],[659.25,0.5],
          [587.33,0.5],[587.33,0.5],[523.25,1.0]
        ];
        var totalDur = mel.reduce(function(s,n){ return s + n[1]*beat; }, 0);

        function scheduleLoop(t0) {
          if (!looping) return;
          var t = t0;
          mel.forEach(function(음표) {
            var dur = note[1] * beat;
            var osc = ctx.createOscillator();
            osc.type = 'square';
            osc.frequency.setValueAtTime(note[0], t);
            var env = ctx.createGain();
            env.gain.setValueAtTime(0.001, t);
            env.gain.linearRampToValueAtTime(1, t + 0.015);
            env.gain.setValueAtTime(1, t + dur - 0.06);
            env.gain.linearRampToValueAtTime(0.001, t + dur - 0.01);
            osc.connect(env);
            env.connect(master);
            osc.start(t);
            osc.stop(t + dur);
            t += dur;
          });
          var tid = setTimeout(function(){
            if (looping) scheduleLoop(ctx.currentTime);
          }, (totalDur - 0.25) * 1000);
          timers.push(tid);
        }
        scheduleLoop(ctx.currentTime + 0.1);

        return {
          stop: function() {
            looping = false;
            master.gain.linearRampToValueAtTime(0, ctx.currentTime + 0.6);
            setTimeout(function(){ try { ctx.close(); } catch(e){} }, 800);
          }
        };
      } catch(e) { return null; }
    }

    // ── Sound Effects ──
    function sfx(type) {
      var AC = window.AudioContext || window.webkitAudioContext;
      if (!AC) return;
      try {
        var ctx = new AC();
        var g = ctx.createGain();
        g.connect(ctx.destination);
        if (type === 'coin') {
          var o = ctx.createOscillator();
          o.type = 'sine';
          o.frequency.setValueAtTime(900, ctx.currentTime);
          o.frequency.exponentialRampToValueAtTime(1500, ctx.currentTime + 0.12);
          g.gain.setValueAtTime(0.3, ctx.currentTime);
          g.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.22);
          o.connect(g); o.start(ctx.currentTime); o.stop(ctx.currentTime + 0.22);
        } else if (type === 'fanfare') {
          [[523.25,0],[659.25,0.12],[783.99,0.24],[1046.5,0.36],[1318.5,0.50]].forEach(function(n){
            var o = ctx.createOscillator();
            o.type = 'square';
            o.frequency.setValueAtTime(n[0], ctx.currentTime + n[1]);
            var ng = ctx.createGain();
            ng.gain.setValueAtTime(0.15, ctx.currentTime + n[1]);
            ng.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + n[1] + 0.18);
            o.connect(ng); ng.connect(ctx.destination);
            o.start(ctx.currentTime + n[1]); o.stop(ctx.currentTime + n[1] + 0.18);
          });
        }
        setTimeout(function(){ try { ctx.close(); } catch(e){} }, 1200);
      } catch(e) {}
    }

    // ── Particle burst ──
    function burstParticles() {
      if (!particles) return;
      particles.innerHTML = '';
      var emojis = ['⭐','✨','🌟','💫','🎊','🎉','🎁','🍀','🎆','🎇'];
      for (var i = 0; i < 28; i++) {
        (function(idx){
          var p = document.createElement('div');
          p.textContent = emojis[idx % emojis.length];
          var tx = (Math.random() * 240 - 120) + 'px';
          var ty = (Math.random() * 220 - 110) + 'px';
          p.style.cssText = [
            'position:absolute','font-size:'+(14+Math.random()*24)+'px',
            'left:50%','top:50%','pointer-events:none',
            '--tx:'+tx,'--ty:'+ty,
            'animation:starFloat 1.1s ease-out '+(Math.random()*0.5)+'s forwards'
          ].join(';');
          particles.appendChild(p);
        })(i);
      }
    }

    // ── Dial spin ──
    function spinDial(turns) {
      if (!dialEl) return;
      var deg = 360 * (turns || 2);
      dialEl.style.transition = 'transform ' + (0.75 * (turns||2)) + 's cubic-bezier(0.4,0,0.2,1)';
      dialEl.style.transform  = 'rotate(' + deg + 'deg)';
      var t = setTimeout(function(){
        dialEl.style.transition = '';
        dialEl.style.transform  = 'rotate(0deg)';
      }, 820 * (turns||2));
      timers.push(t);
    }

    // ── Coin drop ──
    function dropCoin() {
      if (!coinDisplay) return;
      sfx('coin');
      coinDisplay.style.animation = 'none';
      void coinDisplay.offsetWidth;
      coinDisplay.style.animation = 'coinFall 0.65s ease-in-out';
      var t = setTimeout(function(){ coinDisplay.style.animation = ''; }, 750);
      timers.push(t);
    }

    // ── Machine shake ──
    function shake() {
      if (!machineBody) return;
      machineBody.style.animation = 'none';
      void machineBody.offsetWidth;
      machineBody.style.animation = 'shakeX 0.45s ease-in-out';
      var t = setTimeout(function(){ machineBody.style.animation = ''; }, 550);
      timers.push(t);
    }

    // ── Phase transitions ──
    var phaseData = [
      { msg:'🎰 マシン起動中...',           lcd:'▶ LOADING... ◀',   speed:'4s'   },
      { msg:'💰 コインを投入しています...', lcd:'💰 INSERT COIN 💰', speed:'3.5s' },
      { msg:'🌀 ハンドルを回しています...', lcd:'🌀 TURNING... 🌀',  speed:'1.5s' },
      { msg:'🎲 カプセルが転がっています！', lcd:'🎲 GACHA!!! 🎲',   speed:'0.8s' },
      { msg:'✨ もうすぐレシピが出ます...',  lcd:'✨ ALMOST... ✨',   speed:'0.8s' },
      { msg:'🎉 レシピが出た！',             lcd:'🎉 SUCCESS!! 🎉',  speed:'0.5s' }
    ];
    function setPhase(idx) {
      var p = phaseData[Math.min(idx, phaseData.length - 1)];
      if (phaseMsg) phaseMsg.textContent = p.msg;
      if (lcdEl)    lcdEl.textContent    = p.lcd;
      if (spinner)  spinner.style.animationDuration = p.speed;
    }

    // ── Open ──
    function open() {
      if (!overlay) return;
      overlay.classList.remove('hidden');
      setPhase(0);
      musicRef = (typeof SoundManager !== 'undefined') ? SoundManager.startBGM() : startMusic();

      var t1 = setTimeout(function(){ dropCoin(); setPhase(1); }, 600);
      var t2 = setTimeout(function(){ dropCoin(); }, 1300);
      var t3 = setTimeout(function(){ spinDial(2); shake(); setPhase(2); }, 2300);
      var t4 = setTimeout(function(){ spinDial(3); shake(); setPhase(3); }, 3900);
      var t5 = setTimeout(function(){ setPhase(4); shake(); }, 5600);
      timers.push(t1,t2,t3,t4,t5);
    }

    // ── Reveal Result (returns Promise) ──
    function revealResult() {
      setPhase(5);
      if (musicRef) { musicRef.stop(); musicRef = null; }
      sfx('fanfare');
      burstParticles();
      shake();

      if (outputIdle)  outputIdle.style.display  = 'none';
      if (capsuleOut) {
        capsuleOut.style.display   = 'block';
        capsuleOut.style.animation = 'none';
        void capsuleOut.offsetWidth;
        capsuleOut.style.animation = 'capsuleReveal 0.9s cubic-bezier(0.175,0.885,0.32,1.275) forwards';
      }

      return new Promise(function(resolve) {
        var t = setTimeout(function(){
          overlay.classList.add('hidden');
          _reset();
          resolve();
        }, 2400);
        timers.push(t);
      });
    }

    // ── Close (on error) ──
    function close() {
      timers.forEach(function(t){ clearTimeout(t); });
      timers = [];
      if (musicRef) { musicRef.stop(); musicRef = null; }
      overlay.classList.add('hidden');
      _reset();
    }

    function _reset() {
      timers.forEach(function(t){ clearTimeout(t); });
      timers = [];
      if (capsuleOut) { capsuleOut.style.display = 'none'; capsuleOut.style.animation = ''; }
      if (outputIdle) outputIdle.style.display = '';
      if (spinner)    spinner.style.animationDuration = '4s';
      if (dialEl)     { dialEl.style.transition = ''; dialEl.style.transform = ''; }
      if (particles)  particles.innerHTML = '';
    }

    return { open: open, revealResult: revealResult, close: close };
  })();

  // ═══════════════════════════════════════════════════════════════
  //  D A I L Y   C O U N T
  // ═══════════════════════════════════════════════════════════════
  function loadDailyCount() {
    fetch('/gacha/count', {
      credentials: 'same-origin'
    })
        .then(function(r){ return r.json(); })
        .then(function(res){
          if (!res.success) return;
          var d = res.data;
          gachaCount = d.count;
          maxDaily   = d.max;
          gachaCountEl.textContent = gachaCount;
          if (!d.canGenerate) gachaLimitMsg.classList.remove('hidden');
          updateGachaBtn();
        })
        .catch(function(){});
  }

  function showError(msg) {
    errorToastMsg.textContent = msg;
    errorToast.classList.remove('hidden');
    setTimeout(function(){ errorToast.classList.add('hidden'); }, 6000);
  }

  // ═══════════════════════════════════════════════════════════════
  //  I N G R E D I E N T   A P I   I N T E G R A T I O N
  // ═══════════════════════════════════════════════════════════════
  function loadIngredientsFromAPI() {
    fetch('/api/ingredients?type=1', {
      credentials: 'same-origin'
    })
        .then(function(r){ return r.json(); })
        .then(function(res){
          if (res.success && res.data) {
            ingredients = res.data.map(function(item) {
              return { id: item.id, name: item.name, amount: item.quantity, unit: item.unit };
            });
            renderIngredientList(); updateGachaBtn();
          }
        })
        .catch(function(err){ console.error('Failed to load ingredients:', err); });
  }

  function loadSaucesFromAPI() {
    fetch('/api/ingredients?type=2', {
      credentials: 'same-origin'
    })
        .then(function(r){ return r.json(); })
        .then(function(res){
          if (res.success && res.data) {
            sauces = res.data.map(function(item) {
              return { id: item.id, name: item.name, amount: item.quantity, unit: item.unit };
            });
            renderSauceList();
          }
        })
        .catch(function(err){ console.error('Failed to load sauces:', err); });
  }

  function addIngredientToAPI(name, amount, unit, type) {
    return fetch('/api/ingredients', {
      method: 'POST',
      credentials: 'same-origin',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ name: name, quantity: parseFloat(amount), unit: unit, type: type })
    })
        .then(function(r){ return r.json(); });
  }

  function deleteIngredientFromAPI(id) {
    return fetch('/api/ingredients/' + id, {
      method: 'DELETE',
      credentials: 'same-origin'
    })
        .then(function(r){ return r.json(); });
  }

  // ═══════════════════════════════════════════════════════════════
  //  I N G R E D I E N T  /  S A U C E   R E N D E R I N G
  // ═══════════════════════════════════════════════════════════════
  // ── 식재료 리스트 렌더링 함수 ──
  function renderIngredientList() {
    if (ingredients.length === 0) {
      // 재료가 없을 때의 빈 컨테이너 시각화 (냉장고 내부 아이콘 포함)
      // [수정점] 일본어만 노출되도록 빈 냉장고 안내 문구를 '冷蔵庫が空っぽです'로 변경했습니다.
      ingredientList.innerHTML = '<div class="text-center text-blue-300 py-8 opacity-60"><span class="text-4xl block mb-2">🧊</span><p class="text-xs font-bold font-sans">冷蔵庫が空っぽです</p></div>';
    } else {
      // 재료가 있을 때 각 아이템을 '스티커' 스타일의 div로 생성
      ingredientList.innerHTML = ingredients.map(function(item){
        return '<div class="flex items-center justify-between bg-white/70 p-3 rounded-xl border border-blue-100 shadow-sm animate-fade-in-up">'
            + '<span class="font-bold text-slate-700">' + esc(item.name) + '</span>'
            + '<div class="flex items-center gap-2">'
            // 분량 정보 (스티커 내부의 작은 라벨 스타일)
            + '<span class="text-xs bg-blue-100 text-blue-600 px-2 py-1 rounded-md border border-blue-200">' + esc(item.amount) + item.unit + '</span>'
            // 삭제 버튼 (X 아이콘)
            + '<button type="button" class="remove-ingredient text-red-400 hover:text-red-600 p-1 rounded-lg hover:bg-red-50 transition-all" data-id="'+item.id+'">'
            + '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>'
            + '</button></div></div>';
      }).join('');
      ingredientList.querySelectorAll('.remove-ingredient').forEach(function(btn){
        btn.addEventListener('click', function(){
          var id = btn.getAttribute('data-id');
          deleteIngredientFromAPI(id)
              .then(function(res) {
                if (res.success) {
                  ingredients = ingredients.filter(function(i){ return i.id != id; });
                  renderIngredientList(); updateGachaBtn();
                } else {
                  showError(res.message || '食材の削除に失敗しました。');
                }
              })
              .catch(function() { showError('食材削除中にエラーが発生しました。'); });
        });
      });
    }
    ingredientCountEl.textContent = ingredients.length + '個';
  }

  // ── 소스/조미료 리스트 렌더링 함수 ──
  function renderSauceList() {
    if (sauces.length === 0) {
      // 소스가 없을 때의 빈 컨테이너 시각화 (소스통 아이콘 포함)
      // [수정점] 일본어만 노출되도록 빈 소스 안내 문구를 '調味料がありません'으로 변경했습니다.
      sauceList.innerHTML = '<div class="text-center text-orange-300 py-8 opacity-60"><span class="text-4xl block mb-2">🧴</span><p class="text-xs font-bold font-sans">調味料がありません</p></div>';
    } else {
      // 소스가 있을 때 각 아이템을 오렌지 테마의 '스티커' 스타일로 생성
      sauceList.innerHTML = sauces.map(function(item){
        return '<div class="flex items-center justify-between bg-white/70 p-3 rounded-xl border border-orange-100 shadow-sm animate-fade-in-up">'
            + '<span class="font-bold text-slate-700">' + esc(item.name) + '</span>'
            + '<div class="flex items-center gap-2">'
            // 분량 정보
            + '<span class="text-xs bg-orange-100 text-orange-600 px-2 py-1 rounded-md border border-orange-200">' + esc(item.amount) + item.unit + '</span>'
            // 삭제 버튼
            + '<button type="button" class="remove-sauce text-red-400 hover:text-red-600 p-1 rounded-lg hover:bg-red-50 transition-all" data-id="'+item.id+'">'
            + '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>'
            + '</button></div></div>';
      }).join('');
      sauceList.querySelectorAll('.remove-sauce').forEach(function(btn){
        btn.addEventListener('click', function(){
          var id = btn.getAttribute('data-id');
          deleteIngredientFromAPI(id)
              .then(function(res) {
                if (res.success) {
                  sauces = sauces.filter(function(s){ return s.id != id; });
                  renderSauceList();
                } else {
                  showError(res.message || '調味料の削除に失敗しました。');
                }
              })
              .catch(function() { showError('調味料削除中にエラーが発生しました。'); });
        });
      });
    }
    sauceCountEl.textContent = sauces.length + '個';
  }

  function updateGachaBtn() {
    var canGen = gachaCount < maxDaily && ingredients.length > 0;
    gachaBtn.disabled = !canGen;
    if (ingredients.length === 0) gachaWarning.classList.remove('hidden');
    else                          gachaWarning.classList.add('hidden');
  }

  // ═══════════════════════════════════════════════════════════════
  //  A D D   I N G R E D I E N T  /  S A U C E
  // ═══════════════════════════════════════════════════════════════
  document.getElementById('add-ingredient').addEventListener('click', function(){
    // 로그인 체크
    var token = getCookie('accessToken');
    if (!token) {
      showError('ログインが必要です');
      return;
    }
    var name   = document.getElementById('ingredient-name').value.trim();
    var amount = document.getElementById('ingredient-amount').value.trim();
    var unit   = document.getElementById('ingredient-unit').value;

    // [동작 방식] 이미 추가된 재료인지 검사(Duplicate check)합니다.
    // [수정점] 한국어 혼용 경고창을 제거하고 '既に登録されている食材です' 일본어 경고만 띄우도록 변경했습니다.
    var isDuplicate = ingredients.some(function(i) { return i.name === name; });
    if (isDuplicate) {
      showError('既に登録されている食材です');
      return;
    }

    if (name && amount) {
      addIngredientToAPI(name, amount, unit, 1)
          .then(function(res) {
            if (res.success && res.data) {
              ingredients.push({ id: res.data.id, name: res.data.name, amount: res.data.quantity, unit: res.data.unit });
              document.getElementById('ingredient-name').value   = '';
              document.getElementById('ingredient-amount').value = '';
              renderIngredientList(); updateGachaBtn();
            } else {
              showError(res.message || '食材の追加に失敗しました。');
            }
          })
          .catch(function() { showError('食材追加中にエラーが発生しました。'); });
    }
  });
  document.getElementById('ingredient-name').addEventListener('keypress', function(e){
    if (e.key === 'Enter') document.getElementById('add-ingredient').click();
  });

  document.getElementById('add-sauce').addEventListener('click', function(){
    // 로그인 체크
    var token = getCookie('accessToken');
    if (!token) {
      showError('ログインが必要です');
      return;
    }
    var name   = document.getElementById('sauce-name').value.trim();
    var amount = document.getElementById('sauce-amount').value.trim();
    var unit   = document.getElementById('sauce-unit').value;

    // [동작 방식] 이미 추가된 소스인지 검사(Duplicate check)합니다.
    // [수정점] 한국어 혼용 경고창을 제거하고 '既に登録されている調味料です' 일본어 경고만 띄우도록 변경했습니다.
    var isDuplicate = sauces.some(function(s) { return s.name === name; });
    if (isDuplicate) {
      showError('既に登録されている調味料です');
      return;
    }

    if (name && amount) {
      addIngredientToAPI(name, amount, unit, 2)
          .then(function(res) {
            if (res.success && res.data) {
              sauces.push({ id: res.data.id, name: res.data.name, amount: res.data.quantity, unit: res.data.unit });
              document.getElementById('sauce-name').value   = '';
              document.getElementById('sauce-amount').value = '';
              renderSauceList();
            } else {
              showError(res.message || '調味料の追加に失敗しました。');
            }
          })
          .catch(function() { showError('調味料追加中にエラーが発生しました。'); });
    }
  });
  document.getElementById('sauce-name').addEventListener('keypress', function(e){
    if (e.key === 'Enter') document.getElementById('add-sauce').click();
  });

  // ═══════════════════════════════════════════════════════════════
  //  G A C H A   G E N E R A T E
  // ═══════════════════════════════════════════════════════════════
  gachaBtn.addEventListener('click', function(){
    if (gachaCount >= maxDaily || ingredients.length === 0) return;

    gachaResult.classList.add('hidden');
    gachaBtn.disabled = true;
    gachaBtnText.textContent = '🍳 料理中...';

    var MIN_ANIM_MS = 5800;  // minimum machine-animation time (starts AFTER wow)
    var apiDone  = false;
    var animDone = false;
    var apiPayload = null;
    var apiErr     = null;

    function tryReveal() {
      if (!apiDone || !animDone) return;
      if (apiErr) {
        GachaMachine.close();
        showError(apiErr);
        gachaBtn.disabled = (gachaCount >= maxDaily || ingredients.length === 0);
        gachaBtnText.textContent = '🎲 食事ガチャを回す！';
        return;
      }
      // Reveal capsule, then show result card
      GachaMachine.revealResult().then(function(){
        gachaCount++;
        gachaCountEl.textContent = gachaCount;
        if (gachaCount >= maxDaily) gachaLimitMsg.classList.remove('hidden');
        currentRecipeId = apiPayload.id;
        showResult(apiPayload);
        gachaBtn.disabled = (gachaCount >= maxDaily || ingredients.length === 0);
        gachaBtnText.textContent = '🎲 食事ガチャを回す！';
      });
    }

    // ★ WOW pre-sequence (≈2.9 s) → then open machine
    //   MIN_ANIM_MS timer starts AFTER wow so the machine runs its full phases
    WowSequence.run(function () {
      GachaMachine.open();
      setTimeout(function () { animDone = true; tryReveal(); }, MIN_ANIM_MS);
    });

    // API call
    var purpose    = document.querySelector('input[name="purpose"]:checked').value;
    var cuisine    = document.querySelector('input[name="cuisine"]:checked').value;
    var difficulty = document.querySelector('input[name="difficulty"]:checked').value;

    fetch('/gacha/generate', {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ingredients: ingredients.map(function(i){ return { name:i.name, amount:i.amount, unit:i.unit }; }),
        sauces:      sauces.map(function(s){ return { name:s.name, amount:s.amount, unit:s.unit }; }),
        purpose: purpose, cuisine: cuisine, difficulty: difficulty
      })
    })
        .then(function(r){ return r.json(); })
        .then(function(res){
          if (!res.success) {
            apiErr = res.message || 'レシピ生成に失敗しました。';
          } else {
            apiPayload = res.data.recipe;
          }
          apiDone = true;
          tryReveal();
        })
        .catch(function(){
          apiErr = '現在サービスが遅延しております。再読み込みしてからご利用ください。';
          apiDone = true;
          tryReveal();
        });
  });

  // ═══════════════════════════════════════════════════════════════
  //  S H O W   R E S U L T   C A R D
  // ═══════════════════════════════════════════════════════════════
  function showResult(recipe) {
    gachaResult.classList.remove('hidden');

    document.getElementById('result-image').src = recipe.titleImage || '';
    document.getElementById('result-title').textContent    = recipe.title || '';
    document.getElementById('result-summary').textContent  = recipe.summary || '';
    document.getElementById('result-difficulty').textContent = '難易度: ' + (recipe.difficultyLabel || recipe.difficulty || '');
    document.getElementById('result-time-text').textContent  = (recipe.cookingTime || 0) + '分';

    // Nutrients
    var nutrientsEl = document.getElementById('result-nutrients');
    nutrientsEl.innerHTML = '';
    if (recipe.nutrients) {
      var n = recipe.nutrients;
      [
        { label:'カロリー',   value:(n.calories||0)+'kcal', color:'bg-red-50 text-red-700'    },
        { label:'タンパク質', value:(n.protein ||0)+'g',    color:'bg-blue-50 text-blue-700'  },
        { label:'脂質',       value:(n.fat     ||0)+'g',    color:'bg-yellow-50 text-yellow-700' },
        { label:'炭水化物',   value:(n.carbs   ||0)+'g',    color:'bg-green-50 text-green-700' }
      ].forEach(function(item){
        nutrientsEl.innerHTML += '<div class="'+item.color+' p-2 rounded-lg text-center"><div class="text-xs">'+item.label+'</div><div class="font-bold text-sm">'+item.value+'</div></div>';
      });
    }

    // Ingredients
    var ingEl = document.getElementById('result-ingredients-list');
    ingEl.innerHTML = '';
    if (recipe.ingredients) {
      recipe.ingredients.forEach(function(ing){
        ingEl.innerHTML += '<span class="px-2 py-1 bg-amber-100 text-amber-800 rounded-full text-xs">'+esc(ing.name)+' '+(ing.amount||'')+(ing.unit||'')+'</span>';
      });
    }

    document.getElementById('btn-detail').href = '/recipes/' + recipe.id;
    gachaResult.scrollIntoView({ behavior:'smooth', block:'center' });
  }

  // ═══════════════════════════════════════════════════════════════
  //  P U B L I S H   B U T T O N S
  // ═══════════════════════════════════════════════════════════════
  document.getElementById('btn-private').addEventListener('click', function(){
    if (!currentRecipeId) return;
    fetch('/gacha/publish/'+currentRecipeId+'?isPublic=false', {
      method:'POST',
      credentials: 'same-origin'
    })
        .then(function(r){ return r.json(); })
        .then(function(res){
          if (res.success) window.location.href = '/mypage';
          else showError(res.message);
        })
        .catch(function(){ showError('保存に失敗しました。'); });
  });

  document.getElementById('btn-public').addEventListener('click', function(){
    if (!currentRecipeId) return;
    fetch('/gacha/publish/'+currentRecipeId+'?isPublic=true', {
      method:'POST',
      credentials: 'same-origin'
    })
        .then(function(r){ return r.json(); })
        .then(function(res){
          if (res.success) alert('みんなの食卓に登録されました！🎉');
          else showError(res.message);
        })
        .catch(function(){ showError('登録に失敗しました。'); });
  });

  // ═══════════════════════════════════════════════════════════════
  //  I N I T
  // ═══════════════════════════════════════════════════════════════
  setupValidation();  // 입력 검증 초기화
  loadIngredientsFromAPI();
  loadSaucesFromAPI();
  updateGachaBtn();
  loadDailyCount();
})();
