from flask import Flask, request, jsonify
from flask_cors import CORS
from transformers import pipeline

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

# Cache loaded models to improve performance
model_cache = {}

@app.route("/api/translate", methods=["POST"])
def translate():
    try:
        data = request.get_json()
        text = data.get("text")
        model_name = data.get("model")

        if not text or not model_name:
            return jsonify({"error": "Missing 'text' or 'model' parameter"}), 400

        # Load model if not cached already
        if model_name not in model_cache:
            model_cache[model_name] = pipeline("translation", model=model_name)

        translator = model_cache[model_name]
        translated = translator(text)[0]["translation_text"]

        return jsonify({"translation": translated})
    
    except Exception as e:
        print("Translation error:", e)
        return jsonify({"error": "Translation failed"}), 500

if __name__ == "__main__":
    app.run(port=5000)
