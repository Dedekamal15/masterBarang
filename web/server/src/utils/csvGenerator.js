const { Parser } = require('json2csv');

/**
 * Generate CSV string from data array + field definitions
 * @param {Array<Object>} data - Array of row objects
 * @param {Array<{label: string, value: string}>} fields - Field definitions
 * @returns {string} CSV string
 */
function generateCSV(data, fields) {
  if (!data || data.length === 0) {
    return fields.map((f) => f.label).join(',') + '\n';
  }

  const parser = new Parser({
    fields: fields.map((f) => ({
      label: f.label,
      value: f.value,
    })),
    withBOM: true, // Support Excel UTF-8
  });

  return parser.parse(data);
}

module.exports = { generateCSV };
