import React, { useState } from "react";
import { FaMicrophone } from "react-icons/fa";
import "./index.css";

const languagePairs = [
  { label: "English to Hindi", value: "en-hi" },
  { label: "English to French", value: "en-fr" },
  { label: "English to German", value: "en-de" },
  { label: "French to English", value: "fr-en" },
  { label: "Hindi to English", value: "hi-en" },
];

export default function App() {
  const [inputText, setInputText] = useState("");
  const [translatedText, setTranslatedText] = useState("");
  const [selectedLang, setSelectedLang] = useState("en-hi");

  const handleTranslate = async () => {
    if (!inputText.trim()) return;

    const modelMap = {
      "en-hi": "Helsinki-NLP/opus-mt-en-hi",
      "en-fr": "Helsinki-NLP/opus-mt-en-fr",
      "en-de": "Helsinki-NLP/opus-mt-en-de",
      "fr-en": "Helsinki-NLP/opus-mt-fr-en",
      "hi-en": "Helsinki-NLP/opus-mt-hi-en",
    };

    const model = modelMap[selectedLang];

    try {
      const response = await fetch("http://localhost:5000/api/translate", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          text: inputText,
          model: model,
        }),
      });

      const data = await response.json();
      if (data.translation) {
        setTranslatedText(data.translation);
      } else {
        setTranslatedText("Translation failed.");
      }
    } catch (error) {
      console.error("Error during translation:", error);
      setTranslatedText("Error: Could not reach the translation server.");
    }
  };

  const handleVoiceInput = () => {
    alert("Voice input feature would be triggered here.");
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 to-gray-800 text-white font-sans">
      {/* Header */}
      <header className="bg-gray-950 shadow-lg py-4 px-6 flex justify-between items-center sticky top-0 z-50">
        <h1 className="text-2xl font-bold text-blue-400">🌐 Negish App</h1>
        <nav className="space-x-6 text-sm">
          <a href="#" className="text-white hover:text-blue-400 transition">
            Home
          </a>
          <a href="#" className="text-white hover:text-blue-400 transition">
            About
          </a>
          <a href="#" className="text-white hover:text-blue-400 transition">
            Contact
          </a>
        </nav>
      </header>

      {/* Main Content */}
      <div className="max-w-4xl mx-auto rounded-2xl shadow-2xl bg-gray-900 p-10 mt-6">
        <h2 className="text-3xl md:text-4xl font-bold text-center mb-8 text-blue-400">
          Multilingual Voice Translator
        </h2>

        <div className="flex flex-col gap-4">
          <textarea
            className="w-full p-4 rounded-xl bg-gray-800 text-white text-lg outline-none border border-gray-700 focus:ring-2 focus:ring-blue-400 resize-none h-40"
            placeholder="Type or speak something..."
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
          ></textarea>

          <div className="flex flex-col sm:flex-row gap-4 justify-between items-center">
            <select
              value={selectedLang}
              onChange={(e) => setSelectedLang(e.target.value)}
              className="bg-gray-800 p-3 rounded-xl border border-gray-600 focus:ring-2 focus:ring-blue-400 text-white"
            >
              {languagePairs.map((pair, index) => (
                <option key={index} value={pair.value}>
                  {pair.label}
                </option>
              ))}
            </select>

            <div className="flex gap-3 items-center">
              <button
                onClick={handleVoiceInput}
                className="bg-blue-600 hover:bg-blue-700 transition p-3 rounded-full shadow-xl"
              >
                <FaMicrophone className="text-white text-xl" />
              </button>
              <button
                onClick={handleTranslate}
                className="bg-green-600 hover:bg-green-700 transition px-6 py-3 rounded-xl text-white font-semibold shadow-xl"
              >
                Translate
              </button>
            </div>
          </div>

          <div className="bg-gray-800 p-5 rounded-xl mt-6 shadow-inner border border-gray-700">
            <h2 className="text-xl font-semibold mb-2 text-blue-300">
              Translated Output:
            </h2>
            <p className="text-lg text-gray-200 min-h-[40px]">{translatedText}</p>
          </div>
        </div>
      </div>
    </div>
  );
}
