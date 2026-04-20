# ⚡ Express Phone Pro — Android APK

تطبيق أندرويد احترافي لمحل قطع غيار الهواتف

---

## 📱 معلومات التطبيق

| الخاصية | القيمة |
|---------|--------|
| اسم التطبيق | Express Phone Pro |
| Package ID | com.expressphonepro |
| الرابط | https://express-phone-pro.vercel.app/ |
| الحد الأدنى لأندرويد | Android 5.0 (API 21) |
| الإصدار | 1.0.0 |

---

## 🚀 كيف تبني APK مجاناً بدون حاسوب؟

### الخطوة 1 — إنشاء حساب GitHub (مجاني)
1. افتح [github.com](https://github.com)
2. اضغط **Sign up** وأنشئ حساباً مجانياً

### الخطوة 2 — رفع المشروع
1. اضغط **+** ثم **New repository**
2. اسم المستودع: `express-phone-pro-apk`
3. اختر **Public**
4. اضغط **Create repository**
5. ارفع جميع ملفات هذا المشروع

### الخطوة 3 — تشغيل البناء التلقائي
1. افتح مستودعك على GitHub
2. اضغط على تبويب **Actions**
3. ستجد workflow اسمه **Build Express Phone Pro APK**
4. اضغط **Run workflow** ← **Run workflow**
5. انتظر 3-5 دقائق ☕

### الخطوة 4 — تحميل الـ APK
1. بعد انتهاء البناء (✅ أخضر)
2. اضغط على اسم الـ workflow
3. في الأسفل ستجد قسم **Artifacts**
4. حمّل **ExpressPhonePro-Release** 📦
5. فك الضغط وستجد ملف `.apk`

---

## 📲 تثبيت التطبيق على الهاتف

1. انسخ ملف APK إلى هاتفك
2. افتح **الإعدادات** ← **الأمان**
3. فعّل **السماح بمصادر غير معروفة**
4. افتح ملف APK واضغط **تثبيت**

---

## ✨ مميزات التطبيق

- 🖥️ شاشة سبلاش احترافية مع اللوجو
- 🌐 WebView متكامل بكامل مميزات المتصفح
- 📶 رسالة واضحة عند انقطاع الإنترنت
- ↩️ زر الرجوع يعمل داخل الموقع
- 🍪 دعم كامل للـ Cookies
- 📱 تصميم Responsive يتكيف مع جميع الشاشات
- 🌙 ثيم داكن احترافي (Dark Theme)
- ⚡ أيقونة التطبيق من اللوجو الخاص بك

---

## 🛠️ هيكل المشروع

```
ExpressPhonePro/
├── app/
│   ├── src/main/
│   │   ├── java/com/expressphonepro/
│   │   │   └── MainActivity.java      ← المنطق الرئيسي
│   │   ├── res/
│   │   │   ├── layout/activity_main.xml  ← التصميم
│   │   │   ├── drawable/splash_logo.png  ← اللوجو
│   │   │   ├── mipmap-*/ic_launcher.png  ← أيقونات بكل أحجام
│   │   │   └── values/                   ← الألوان والثيم
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── .github/workflows/build-apk.yml    ← بناء تلقائي مجاني
└── README.md
```

---

## ❓ مشاكل شائعة

**البناء فشل في GitHub Actions؟**
- تأكد من رفع جميع الملفات بما فيها مجلد `gradle/`

**التطبيق لا يفتح الموقع؟**
- تأكد من الاتصال بالإنترنت
- تأكد أن الموقع يعمل على `https://express-phone-pro.vercel.app/`

---

صُنع بـ ❤️ لمحل Express Phone Pro
